import { getOrders } from "./api.js";
import { showAuthenticationRequired } from "./auth.js";
import { updateCartBadge } from "./cart-state.js";

const elements = {
    auth: document.querySelector("#ordersAuth"),
    empty: document.querySelector("#ordersEmpty"),
    emptyMessage: document.querySelector("#ordersEmptyMessage"),
    emptyTitle: document.querySelector("#ordersEmptyTitle"),
    error: document.querySelector("#ordersError"),
    errorMessage: document.querySelector("#ordersErrorMessage"),
    grid: document.querySelector("#ordersGrid"),
    loading: document.querySelector("#ordersLoading"),
    noResults: document.querySelector("#ordersNoResults"),
    search: document.querySelector("#orderSearch"),
    count: document.querySelector("#ordersCount")
};

let orders = [];

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

function validateOrdersResponse(data) {
    if (data === null || data === undefined) return [];

    if (!Array.isArray(data)) {
        throw new Error("The orders response was not a list.");
    }

    return data.map((order) => {
        if (
            !order ||
            !Number.isInteger(order.id) ||
            order.id < 1 ||
            (order.productId !== null &&
                (!Number.isInteger(order.productId) || order.productId < 1)) ||
            (order.quantity !== null &&
                (!Number.isInteger(order.quantity) || order.quantity < 1)) ||
            typeof order.status !== "string" ||
            order.status.trim().length === 0
        ) {
            throw new Error("The orders response had an unexpected shape.");
        }

        return {
            id: order.id,
            productId: order.productId,
            quantity: order.quantity,
            status: order.status.trim()
        };
    });
}

function getStatusStyle(status) {
    const normalisedStatus = status.toUpperCase();
    if (normalisedStatus === "PAID") return { className: "order-status--paid", icon: "bi-check-circle" };
    if (normalisedStatus === "CANCELLED") return { className: "order-status--cancelled", icon: "bi-x-circle" };
    if (normalisedStatus === "CREATED") return { className: "order-status--created", icon: "bi-clock" };
    return { className: "order-status--default", icon: "bi-info-circle" };
}

function createOrderCard(order) {
    const card = document.createElement("article");
    const header = document.createElement("div");
    const titleGroup = document.createElement("div");
    const label = createTextElement("p", "order-card__label", "Order record");
    const title = createTextElement("h2", "order-card__title", `Order #${order.id}`);
    const status = document.createElement("span");
    const statusStyle = getStatusStyle(order.status);
    const facts = document.createElement("div");
    const productFact = document.createElement("div");
    const quantityFact = document.createElement("div");
    const productLabel = createTextElement("span", "order-card__fact-label", "Product ID");
    const productValue = createTextElement("strong", "order-card__fact-value", order.productId === null ? "N/A" : String(order.productId));
    const quantityLabel = createTextElement("span", "order-card__fact-label", "Quantity");
    const quantityValue = createTextElement("strong", "order-card__fact-value", order.quantity === null ? "N/A" : String(order.quantity));
    const footer = document.createElement("div");
    const statusIcon = createIcon(`bi ${statusStyle.icon}`);
    const detailsLink = document.createElement("a");
    const buttonIcon = createIcon("bi bi-arrow-up-right");

    card.className = "order-card";
    header.className = "order-card__header";
    titleGroup.append(label, title);
    status.className = `order-status ${statusStyle.className}`;
    status.append(statusIcon, document.createTextNode(order.status));
    status.setAttribute("aria-label", `Status: ${order.status}`);
    header.append(titleGroup, status);
    facts.className = "order-card__facts";
    productFact.append(productLabel, productValue);
    quantityFact.append(quantityLabel, quantityValue);
    facts.append(productFact, quantityFact);
    footer.className = "order-card__footer";
    detailsLink.className = "order-details-button";
    detailsLink.href = `order-details.html?id=${encodeURIComponent(order.id)}`;
    detailsLink.append(document.createTextNode("View Details"), buttonIcon);
    footer.append(createTextElement("span", "order-card__note", "Status is managed by the backend"), detailsLink);
    card.append(header, facts, footer);
    return card;
}

function renderOrders(records) {
    elements.grid.replaceChildren(...records.map(createOrderCard));
    setVisible(elements.grid, records.length > 0);
    setVisible(elements.noResults, records.length === 0 && orders.length > 0);
    elements.count.textContent = `${records.length} ${records.length === 1 ? "order" : "orders"}`;
}

function filterOrders() {
    const query = elements.search.value.trim().toLowerCase();

    const filteredOrders = orders
        .filter((order) =>
            String(order.id).includes(query) ||
            String(order.productId).includes(query) ||
            order.status.toLowerCase().includes(query)
        )
        .sort((a, b) => b.id - a.id);

    renderOrders(filteredOrders);
}

function hideAllStates() {
    [elements.loading, elements.empty, elements.error, elements.auth, elements.noResults, elements.grid].forEach((element) => setVisible(element, false));
}

function showError(error) {
    hideAllStates();
    elements.errorMessage.textContent = error.message || "We could not load your orders right now.";
    setVisible(elements.error, true);
    elements.count.textContent = "Orders unavailable";
}

function showAuthenticationRequiredState() {
    hideAllStates();
    setVisible(elements.auth, true);
    elements.count.textContent = "Authentication required";
}

async function loadOrders() {
    hideAllStates();
    setVisible(elements.loading, true);
    elements.count.textContent = "Loading orders...";
    try {
        const response = await getOrders();
        orders = validateOrdersResponse(response);
        hideAllStates();
        if (orders.length === 0) {
            elements.emptyTitle.textContent = response === null ? "Orders are unavailable" : "No orders yet";
            elements.emptyMessage.textContent = response === null ? "The Order API returned no order list." : "Orders you place will appear here.";
            setVisible(elements.empty, true);
            elements.count.textContent = "0 orders";
            return;
        }
        filterOrders();
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
elements.search.addEventListener("input", filterOrders);
document.querySelector("#refreshOrders").addEventListener("click", loadOrders);
document.querySelector("#retryOrders").addEventListener("click", loadOrders);
loadOrders();

