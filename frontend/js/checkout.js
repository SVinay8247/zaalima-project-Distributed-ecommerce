import { createOrder } from "./api.js";
import { readCart, removeCartItem } from "./cart-state.js";
import { updateCartBadge } from "./cart-state.js";
import { showAuthenticationRequired } from "./auth.js";

const elements = {
    auth: document.querySelector("#checkoutAuth"),
    content: document.querySelector("#checkoutContent"),
    empty: document.querySelector("#checkoutEmpty"),
    error: document.querySelector("#checkoutError"),
    errorMessage: document.querySelector("#checkoutErrorMessage"),
    item: document.querySelector("#checkoutItem"),
    loading: document.querySelector("#checkoutLoading"),
    multiple: document.querySelector("#checkoutMultiple"),
    orderResponse: document.querySelector("#orderResponse"),
    placeOrder: document.querySelector("#placeOrder"),
    success: document.querySelector("#checkoutSuccess"),
    summaryProductId: document.querySelector("#summaryProductId"),
    summaryQuantity: document.querySelector("#summaryQuantity"),
    summaryInventoryId: document.querySelector("#summaryInventoryId")
};

let checkoutItem = null;

function setVisible(element, visible) {
    element.classList.toggle("d-none", !visible);
}

function hideAllStates() {
    [elements.loading, elements.empty, elements.multiple, elements.error, elements.auth, elements.success, elements.content].forEach((element) => setVisible(element, false));
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

function validateCartItem(item) {
    return item && item.inventoryId !== null && item.inventoryId !== undefined && Number.isInteger(item.productId) && item.productId > 0 && Number.isInteger(item.quantity) && item.quantity > 0;
}

function renderCheckoutItem(item) {
    const visual = document.createElement("div");
    const body = document.createElement("div");
    const label = createTextElement("p", "checkout-item__label", "Inventory record");
    const title = createTextElement("h2", "checkout-item__title", `Product ${item.productId}`);
    const details = document.createElement("div");
    const productId = createTextElement("span", "checkout-item__meta", `Product ID: ${item.productId}`);
    const inventoryId = createTextElement("span", "checkout-item__meta", `Inventory ID: ${item.inventoryId}`);
    const quantity = createTextElement("strong", "checkout-item__quantity", `${item.quantity}`);
    const quantityLabel = createTextElement("span", "checkout-item__quantity-label", "units");

    visual.className = "checkout-item__visual";
    visual.append(createIcon("bi bi-box-seam"));
    body.className = "checkout-item__body";
    details.className = "checkout-item__details";
    details.append(productId, inventoryId);
    body.append(label, title, details);
    const quantityBlock = document.createElement("div");
    quantityBlock.className = "checkout-item__quantity-block";
    quantityBlock.append(quantity, quantityLabel);
    body.append(quantityBlock);
    elements.item.replaceChildren(visual, body);
    elements.summaryProductId.textContent = String(item.productId);
    elements.summaryQuantity.textContent = String(item.quantity);
    elements.summaryInventoryId.textContent = String(item.inventoryId);
}

function showReview(item) {
    checkoutItem = item;
    hideAllStates();
    renderCheckoutItem(item);
    setVisible(elements.content, true);
}

function showSimpleState(element) {
    hideAllStates();
    setVisible(element, true);
}

function showError(error) {
    hideAllStates();
    elements.errorMessage.textContent = error.message || "We could not place this order. Please try again.";
    setVisible(elements.error, true);
}

function showAuthenticationState() {
    hideAllStates();
    setVisible(elements.auth, true);
}

function validateOrderResponse(data) {
    if (!data || typeof data !== "object" || Array.isArray(data) || data.id === null || data.id === undefined || data.productId === null || data.productId === undefined || !Number.isInteger(data.quantity) || data.quantity <= 0 || typeof data.status !== "string" || data.status.length === 0) {
        throw new Error("The Order API returned an unexpected response.");
    }
    return { id: data.id, productId: data.productId, quantity: data.quantity, status: data.status };
}

function renderOrderResponse(order) {
    elements.orderResponse.replaceChildren();
    const fields = [["Order ID", order.id], ["Product ID", order.productId], ["Quantity", order.quantity], ["Backend status", order.status]];
    fields.forEach(([label, value]) => {
        const row = document.createElement("div");
        row.className = "order-response__row";
        row.append(createTextElement("span", "order-response__label", label), createTextElement("strong", "order-response__value", String(value)));
        elements.orderResponse.append(row);
    });
}

async function placeOrder() {
    const items = readCart();
    if (items.length === 0) {
        showSimpleState(elements.empty);
        return;
    }
    if (items.length > 1) {
        showSimpleState(elements.multiple);
        return;
    }
    const item = items[0];
    if (!validateCartItem(item)) {
        showError(new Error("The saved cart item is invalid."));
        return;
    }

    elements.placeOrder.disabled = true;
    elements.placeOrder.replaceChildren(createIcon("spinner-border spinner-border-sm me-2"), document.createTextNode("Placing Order..."));
    try {
        const order = validateOrderResponse(await createOrder(item.productId, item.quantity));
        removeCartItem(item.inventoryId);
        renderOrderResponse(order);
        hideAllStates();
        setVisible(elements.success, true);
    } catch (error) {
        if (error.status === 401 || error.status === 403) showAuthenticationState();
        else showError(error);
    } finally {
        elements.placeOrder.disabled = false;
        elements.placeOrder.replaceChildren(createIcon("bi bi-send me-2"), document.createTextNode("Place Order"));
    }
}

function loadCheckout() {
    hideAllStates();
    setVisible(elements.loading, true);
    window.setTimeout(() => {
        const items = readCart();
        if (items.length === 0) showSimpleState(elements.empty);
        else if (items.length > 1) showSimpleState(elements.multiple);
        else if (!validateCartItem(items[0])) showError(new Error("The saved cart item is invalid."));
        else showReview(items[0]);
    }, 0);
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
document.querySelector("#retryCheckout").addEventListener("click", loadCheckout);
elements.placeOrder.addEventListener("click", placeOrder);
loadCheckout();
