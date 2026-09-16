import { getInventoryById } from "./api.js";
import { showAuthenticationRequired } from "./auth.js";
import { addCartItem, readCart, updateCartBadge } from "./cart-state.js";

const elements = {
    content: document.querySelector("#detailContent"),
    error: document.querySelector("#detailError"),
    errorMessage: document.querySelector("#detailErrorMessage"),
    loading: document.querySelector("#detailLoading"),
    notFound: document.querySelector("#detailNotFound"),
    notFoundMessage: document.querySelector("#detailNotFoundMessage"),
    auth: document.querySelector("#detailAuth"),
    quantity: document.querySelector("#requestedQuantity"),
    decrease: document.querySelector("#decreaseQuantity"),
    increase: document.querySelector("#increaseQuantity")
};
const addToCartButton = document.querySelector("#addToCart");
let currentRecord = null;

const inventoryId = new URLSearchParams(window.location.search).get("id");

function setVisible(element, visible) {
    element.classList.toggle("d-none", !visible);
}

function setBaseState() {
    setVisible(elements.loading, false);
    setVisible(elements.error, false);
    setVisible(elements.notFound, false);
    setVisible(elements.auth, false);
    setVisible(elements.content, false);
}

function getAvailability(quantity) {
    if (quantity === 0) return { className: "availability--empty", label: "Out of stock", icon: "bi-x-circle" };
    if (quantity <= 5) return { className: "availability--limited", label: "Limited availability", icon: "bi-exclamation-circle" };
    return { className: "availability--available", label: "In stock", icon: "bi-check-circle" };
}

function validateInventoryResponse(data) {
    if (data === null || data === undefined) return null;
    if (typeof data !== "object" || Array.isArray(data) || data.id === null || data.id === undefined || data.productId === null || data.productId === undefined || !Number.isInteger(data.quantity) || data.quantity < 0) {
        throw new Error("The inventory response had an unexpected shape.");
    }
    return { id: data.id, productId: data.productId, quantity: data.quantity };
}

function renderDetails(record) {
    currentRecord = record;
    const availability = getAvailability(record.quantity);
    document.querySelectorAll("[data-product-id]").forEach((element) => { element.textContent = record.productId; });
    document.querySelector("[data-inventory-id]").textContent = record.id;
    document.querySelector("[data-quantity]").textContent = `${record.quantity} units`;
    const availabilityElement = document.querySelector("[data-availability]");
    availabilityElement.className = `detail-status availability ${availability.className}`;
    availabilityElement.replaceChildren();
    const icon = document.createElement("i");
    icon.className = `bi ${availability.icon}`;
    icon.setAttribute("aria-hidden", "true");
    availabilityElement.append(icon, document.createTextNode(availability.label));
    elements.quantity.max = Math.max(record.quantity, 1);
    elements.quantity.disabled = record.quantity === 0;
    elements.decrease.disabled = record.quantity === 0;
    elements.increase.disabled = record.quantity === 0;
    addToCartButton.disabled = record.quantity === 0;
    setVisible(elements.content, true);
}

function addCurrentItemToCart() {
    if (!currentRecord || addToCartButton.disabled) return;
    const requestedQuantity = Number(elements.quantity.value);
    if (!Number.isInteger(requestedQuantity) || requestedQuantity < 1 || requestedQuantity > currentRecord.quantity) {
        elements.quantity.value = String(Math.max(1, currentRecord.quantity));
        showCartWarning(`Choose between 1 and ${currentRecord.quantity} available units.`);
        return;
    }
    const existingItem = readCart().find((item) => String(item.inventoryId) === String(currentRecord.id));
    const existingQuantity = existingItem ? existingItem.quantity : 0;
    const remainingQuantity = currentRecord.quantity - existingQuantity;
    if (remainingQuantity <= 0) {
        showCartWarning("This inventory item is already at the available quantity in your cart.");
        return;
    }
    const quantityToAdd = Math.min(requestedQuantity, remainingQuantity);
    addCartItem({ inventoryId: currentRecord.id, productId: currentRecord.productId }, quantityToAdd);
    updateCartBadge();
    if (quantityToAdd < requestedQuantity) {
        showCartWarning(`Only ${remainingQuantity} more unit${remainingQuantity === 1 ? "" : "s"} can be added.`);
        return;
    }
    const messageElement = document.querySelector("[data-toast-message]");
    messageElement.textContent = "Inventory item added to your cart.";
    new bootstrap.Toast(document.querySelector("#appToast"), { delay: 3600 }).show();
}

function showCartWarning(message) {
    const messageElement = document.querySelector("[data-toast-message]");
    messageElement.textContent = message;
    new bootstrap.Toast(document.querySelector("#appToast"), { delay: 3600 }).show();
}

function showNotFound(message) {
    setBaseState();
    elements.notFoundMessage.textContent = message;
    setVisible(elements.notFound, true);
}

function showError(error) {
    setBaseState();
    elements.errorMessage.textContent = error.message || "We could not load this inventory record. Please try again.";
    setVisible(elements.error, true);
}

function showAuthenticationState() {
    setBaseState();
    setVisible(elements.auth, true);
}

async function loadDetails() {
    setBaseState();
    setVisible(elements.loading, true);
    if (!inventoryId || !/^\d+$/.test(inventoryId)) {
        showNotFound("A valid inventory ID was not provided in the URL.");
        return;
    }
    try {
        const record = validateInventoryResponse(await getInventoryById(inventoryId));
        if (!record) {
            showNotFound("We could not find an inventory item with that ID.");
            return;
        }
        renderDetails(record);
    } catch (error) {
        if (error.status === 401 || error.status === 403) showAuthenticationState();
        else showError(error);
    }
}

function updateQuantity(change) {
    if (elements.quantity.disabled) return;
    const minimum = Number(elements.quantity.min);
    const maximum = Number(elements.quantity.max);
    const nextValue = Math.min(maximum, Math.max(minimum, Number(elements.quantity.value) + change));
    elements.quantity.value = Number.isFinite(nextValue) ? nextValue : minimum;
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
elements.decrease.addEventListener("click", () => updateQuantity(-1));
elements.increase.addEventListener("click", () => updateQuantity(1));
addToCartButton.addEventListener("click", addCurrentItemToCart);
document.querySelector("#retryDetail").addEventListener("click", loadDetails);
loadDetails();
