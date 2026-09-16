export const CART_STORAGE_KEY = "zaalima-cart";

function isValidIdentifier(value) {
    return (typeof value === "number" && Number.isFinite(value)) || (typeof value === "string" && value.trim().length > 0);
}

function isValidQuantity(quantity) {
    return Number.isInteger(quantity) && quantity > 0;
}

function normaliseItem(item) {
    if (!item || !isValidIdentifier(item.inventoryId) || !isValidIdentifier(item.productId) || !isValidQuantity(item.quantity)) return null;
    return {
        inventoryId: item.inventoryId,
        productId: item.productId,
        quantity: item.quantity
    };
}

function saveCart(items) {
    try {
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
    } catch {
        // Keep the current page usable when browser storage is unavailable.
    }
    return items;
}

export function readCart() {
    try {
        const stored = localStorage.getItem(CART_STORAGE_KEY);
        if (!stored) return [];
        const parsed = JSON.parse(stored);
        if (!Array.isArray(parsed)) throw new Error("Cart data was not a list.");
        const validItems = parsed.map(normaliseItem).filter(Boolean);
        if (validItems.length !== parsed.length) return saveCart([]);
        return validItems;
    } catch {
        return saveCart([]);
    }
}

export function addCartItem(item, quantity) {
    const normalisedItem = normaliseItem({ ...item, quantity });
    if (!normalisedItem) return readCart();
    const items = readCart();
    const existingItem = items.find((cartItem) => String(cartItem.inventoryId) === String(normalisedItem.inventoryId));
    if (existingItem) {
        existingItem.quantity += normalisedItem.quantity;
    } else {
        items.push(normalisedItem);
    }
    return saveCart(items);
}

export function updateCartItem(inventoryId, quantity) {
    const items = readCart();
    if (!isValidQuantity(quantity)) return items;
    const item = items.find((cartItem) => String(cartItem.inventoryId) === String(inventoryId));
    if (!item) return items;
    item.quantity = quantity;
    return saveCart(items);
}

export function removeCartItem(inventoryId) {
    return saveCart(readCart().filter((item) => String(item.inventoryId) !== String(inventoryId)));
}

export function clearCart() {
    return saveCart([]);
}

export function getCartItemCount(items = readCart()) {
    return items.reduce((total, item) => total + item.quantity, 0);
}

export function updateCartBadge(items = readCart()) {
    const badge = document.querySelector(".cart-count");
    if (!badge) return;
    const count = getCartItemCount(items);
    badge.textContent = String(count);
    badge.setAttribute("aria-label", `${count} items`);
}
