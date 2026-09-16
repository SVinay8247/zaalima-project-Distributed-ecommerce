import { clearCart, getCartItemCount, readCart, removeCartItem, updateCartBadge, updateCartItem } from "./cart-state.js";
import { showAuthenticationRequired } from "./auth.js";

const elements = {
    cartContent: document.querySelector("#cartContent"),
    cartItems: document.querySelector("#cartItems"),
    emptyCart: document.querySelector("#emptyCart"),
    summaryItemCount: document.querySelector("#summaryItemCount"),
    summaryRecordCount: document.querySelector("#summaryRecordCount")
};

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

function createCartItem(item) {
    const card = document.createElement("article");
    const visual = document.createElement("div");
    const body = document.createElement("div");
    const label = createTextElement("p", "cart-item__label", "Inventory record");
    const title = createTextElement("h2", "cart-item__title", `Product ${item.productId}`);
    const metadata = document.createElement("div");
    const inventoryId = createTextElement("span", "cart-item__meta", `Inventory ID: ${item.inventoryId}`);
    const productId = createTextElement("span", "cart-item__meta", `Product ID: ${item.productId}`);
    const controls = document.createElement("div");
    const quantityLabel = createTextElement("label", "cart-item__quantity-label", "Quantity");
    const stepper = document.createElement("div");
    const decrease = document.createElement("button");
    const quantity = document.createElement("input");
    const increase = document.createElement("button");
    const remove = document.createElement("button");
    const icon = document.createElement("i");

    card.className = "cart-item";
    visual.className = "cart-item__visual";
    body.className = "cart-item__body";
    metadata.className = "cart-item__metadata";
    controls.className = "cart-item__controls";
    quantityLabel.setAttribute("for", `cart-quantity-${item.inventoryId}`);
    stepper.className = "cart-stepper";
    decrease.className = "cart-stepper__button";
    decrease.type = "button";
    decrease.setAttribute("aria-label", `Decrease quantity for product ${item.productId}`);
    decrease.append(createIcon("bi bi-dash"));
    quantity.className = "cart-stepper__input";
    quantity.id = `cart-quantity-${item.inventoryId}`;
    quantity.type = "number";
    quantity.min = "1";
    quantity.value = String(item.quantity);
    quantity.inputMode = "numeric";
    increase.className = "cart-stepper__button";
    increase.type = "button";
    increase.setAttribute("aria-label", `Increase quantity for product ${item.productId}`);
    increase.append(createIcon("bi bi-plus"));
    remove.className = "cart-item__remove";
    remove.type = "button";
    remove.setAttribute("aria-label", `Remove product ${item.productId} from cart`);
    remove.title = "Remove item";
    icon.className = "bi bi-trash3";
    icon.setAttribute("aria-hidden", "true");

    visual.append(createIcon("bi bi-box-seam"));
    metadata.append(inventoryId, productId);
    body.append(label, title, metadata);
    decrease.addEventListener("click", () => changeQuantity(item, -1));
    increase.addEventListener("click", () => changeQuantity(item, 1));
    quantity.addEventListener("change", () => setQuantity(item, quantity.value));
    remove.addEventListener("click", () => { removeCartItem(item.inventoryId); renderCart(); });
    stepper.append(decrease, quantity, increase);
    controls.append(quantityLabel, stepper);
    remove.append(icon);
    card.append(visual, body, controls, remove);
    return card;
}

function changeQuantity(item, change) {
    const nextQuantity = item.quantity + change;
    if (nextQuantity > 0) updateCartItem(item.inventoryId, nextQuantity);
    renderCart();
}

function setQuantity(item, rawQuantity) {
    const parsedQuantity = Number(rawQuantity);
    if (Number.isInteger(parsedQuantity) && parsedQuantity > 0) updateCartItem(item.inventoryId, parsedQuantity);
    renderCart();
}

function renderCart() {
    const items = readCart();
    const hasItems = items.length > 0;
    elements.cartItems.replaceChildren(...items.map(createCartItem));
    elements.cartContent.classList.toggle("d-none", !hasItems);
    elements.emptyCart.classList.toggle("d-none", hasItems);
    elements.summaryItemCount.textContent = String(getCartItemCount(items));
    elements.summaryRecordCount.textContent = String(items.length);
    updateCartBadge(items);
}

document.querySelector("[data-current-year]").textContent = new Date().getFullYear();
document.querySelector("[data-action='account']")?.addEventListener("click", () => {
    showAuthenticationRequired((message) => {
        const messageElement = document.querySelector("[data-toast-message]");
        messageElement.textContent = message;
        new bootstrap.Toast(document.querySelector("#appToast"), { delay: 3600 }).show();
    });
});
document.querySelector("#clearCart").addEventListener("click", () => { clearCart(); renderCart(); });
renderCart();
