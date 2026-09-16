import { showAuthenticationRequired } from "./auth.js";
import { updateCartBadge } from "./cart-state.js";

document.querySelector("[data-current-year]").textContent = new Date().getFullYear();
updateCartBadge();
document.querySelectorAll("[data-action='account']").forEach((button) => {
    button.addEventListener("click", () => {
        showAuthenticationRequired((message) => {
            const messageElement = document.querySelector("[data-toast-message]");
            messageElement.textContent = message;
            new bootstrap.Toast(document.querySelector("#appToast"), { delay: 3600 }).show();
        });
    });
});
