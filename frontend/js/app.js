import { showAuthenticationRequired } from "./auth.js";
import { updateCartBadge } from "./cart-state.js";

const toastElement = document.querySelector("#appToast");
const toastMessage = document.querySelector("[data-toast-message]");
const toast = toastElement && window.bootstrap ? new bootstrap.Toast(toastElement, { delay: 3600 }) : null;

function showToast(message) {
    if (!toast || !toastMessage) return;
    toastMessage.textContent = message;
    toast.show();
}

document.querySelector("[data-current-year]").textContent = new Date().getFullYear();
updateCartBadge();
document.querySelectorAll("[data-action='account']").forEach((button) => {
    button.addEventListener("click", () => showAuthenticationRequired(showToast));
});
document.querySelectorAll("[data-action='search']").forEach((button) => {
    button.addEventListener("click", () => showToast("Product search will be available with the catalog page."));
});
