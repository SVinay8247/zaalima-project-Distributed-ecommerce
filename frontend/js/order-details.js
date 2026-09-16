import { getOrderById } from "./api.js";
import { showAuthenticationRequired } from "./auth.js";
import { updateCartBadge } from "./cart-state.js";

const elements = {
    auth: document.querySelector("#orderAuth"),
    content: document.querySelector("#orderContent"),
    error: document.querySelector("#orderError"),
    errorMessage: document.querySelector("#orderErrorMessage"),
    loading: document.querySelector("#orderLoading"),
    notFound: document.querySelector("#orderNotFound"),
    notFoundMessage: document.querySelector("#orderNotFoundMessage"),
    status: document.querySelector("[data-status]"),
    statusBadge: document.querySelector("[data-status-badge]"),
    tracker: document.querySelector("#statusTracker")
};

const orderId = new URLSearchParams(window.location.search).get("id");
let currentOrder = null;

function setVisible(element, visible) {
    element.classList.toggle("d-none", !visible);
}

function createTextElement(tagName, className, text) {
    const element = document.createElement(tagName);
    element.className = className;
    element.textContent = text;
    return element;
}

function createIcon(className) {
    const icon = document.createElement("i");
    icon.className = className;
    icon.setAttribute("aria-hidden", "true");
    return icon;
}

function hideAllStates() {
    [elements.loading, elements.notFound, elements.error, elements.auth, elements.content].forEach((element) => setVisible(element, false));
}

function validateOrderResponse(data) {
    if (data === null || data === undefined) return null;
    if (typeof data !== "object" || Array.isArray(data) || !Number.isInteger(data.id) || data.id < 1 || !Number.isInteger(data.productId) || data.productId < 1 || !Number.isInteger(data.quantity) || data.quantity < 1 || typeof data.status !== "string" || data.status.trim().length === 0) {
        throw new Error("The order response had an unexpected shape.");
    }
    return { id: data.id, productId: data.productId, quantity: data.quantity, status: data.status.trim() };
}

function getStatusStyle(status) {
    const normalisedStatus = status.toUpperCase();
    if (normalisedStatus === "PAID") return { className: "order-status--paid", icon: "bi-check-circle", label: "PAID" };
    if (normalisedStatus === "CANCELLED") return { className: "order-status--cancelled", icon: "bi-x-circle", label: "CANCELLED" };
    if (normalisedStatus === "CREATED") return { className: "order-status--created", icon: "bi-clock", label: "CREATED" };
    return { className: "order-status--default", icon: "bi-info-circle", label: status };
}

function createTrackerStep(label, state, iconClass) {
    const step = document.createElement("div");
    const marker = document.createElement("span");
    const title = createTextElement("strong", "status-tracker__step-title", label);
    const stateLabel = createTextElement("span", "status-tracker__step-state", state === "current" ? "Current status" : "Completed status");
    step.className = `status-tracker__step status-tracker__step--${state}`;
    marker.className = "status-tracker__marker";
    marker.append(createIcon(`bi ${iconClass}`));
    step.append(marker, title, stateLabel);
    return step;
}

function renderTracker(status) {
    const normalisedStatus = status.toUpperCase();
    const steps = normalisedStatus === "PAID"
        ? [["Order Created", "completed", "bi-check2"], ["Order Completed", "current", "bi-check-circle"]]
        : normalisedStatus === "CANCELLED"
            ? [["Order Created", "completed", "bi-check2"], ["Order Cancelled", "current", "bi-x-circle"]]
            : normalisedStatus === "CREATED"
                ? [["Order Created", "current", "bi-clock"]]
                : [[status, "current", "bi-info-circle"]];
    elements.tracker.replaceChildren(...steps.map(([label, state, icon]) => createTrackerStep(label, state, icon)));
}

function renderOrder(order) {
    currentOrder = order;
    const statusStyle = getStatusStyle(order.status);
    document.querySelectorAll("[data-order-id]").forEach((element) => { element.textContent = String(order.id); });
    document.querySelector("[data-product-id]").textContent = String(order.productId);
    document.querySelector("[data-quantity]").textContent = String(order.quantity);
    elements.status.textContent = statusStyle.label;
    elements.statusBadge.className = `order-status ${statusStyle.className}`;
    elements.statusBadge.replaceChildren(createIcon(`bi ${statusStyle.icon}`), document.createTextNode(statusStyle.label));
    elements.statusBadge.setAttribute("aria-label", `Status: ${statusStyle.label}`);
    renderTracker(order.status);
    hideAllStates();
    setVisible(elements.content, true);
}

function showNotFound(message) {
    hideAllStates();
    elements.notFoundMessage.textContent = message;
    setVisible(elements.notFound, true);
}

function showError(error) {
    hideAllStates();
    elements.errorMessage.textContent = error.message || "We could not load this order right now.";
    setVisible(elements.error, true);
}

function showAuthenticationRequiredState() {
    hideAllStates();
    setVisible(elements.auth, true);
}

async function loadOrder() {
    hideAllStates();
    setVisible(elements.loading, true);
    if (!orderId || !/^\d+$/.test(orderId) || Number(orderId) < 1) {
        showNotFound("A valid order ID was not provided in the URL.");
        return;
    }
    try {
        const order = validateOrderResponse(await getOrderById(orderId));
        if (!order) {
            showNotFound("We could not find an order with that ID.");
            return;
        }
        renderOrder(order);
    } catch (error) {
        if (error.status === 401 || error.status === 403) showAuthenticationRequiredState();
        else showError(error);
    }
}

document.querySelector("[data-current-year]").textContent = new Date().getFullYear();
updateCartBadge();
document.querySelector("[data-action='account']")?.addEventListener("click", () => {
    showAuthenticationRequired((message) => {
        const messageElement = document.querySelector("[data-toast-message]");
        messageElement.textContent = message;
        new bootstrap.Toast(document.querySelector("#appToast"), { delay: 3600 }).show();
    });
});
document.querySelector("#retryOrder").addEventListener("click", loadOrder);
document.querySelector("#refreshOrder").addEventListener("click", loadOrder);
loadOrder();
