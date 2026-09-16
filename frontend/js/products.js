import { getInventory } from "./api.js";
import { showAuthenticationRequired } from "./auth.js";
import { updateCartBadge } from "./cart-state.js";

const elements = {
    error: document.querySelector("#inventoryError"),
    errorMessage: document.querySelector("#inventoryErrorMessage"),
    auth: document.querySelector("#inventoryAuth"),
    empty: document.querySelector("#inventoryEmpty"),
    grid: document.querySelector("#inventoryGrid"),
    loading: document.querySelector("#inventoryLoading"),
    noResults: document.querySelector("#inventoryNoResults"),
    resultsCount: document.querySelector("#resultsCount"),
    search: document.querySelector("#inventorySearch")
};

let inventoryRecords = [];

function setVisible(element, visible) {
    element.classList.toggle("d-none", !visible);
}

function createTextElement(tagName, className, text) {
    const element = document.createElement(tagName);
    element.className = className;
    element.textContent = text;
    return element;
}

function getAvailability(quantity) {
    if (quantity === 0) return { className: "availability--empty", label: "Out of stock", icon: "bi-x-circle" };
    if (quantity <= 5) return { className: "availability--limited", label: "Limited availability", icon: "bi-exclamation-circle" };
    return { className: "availability--available", label: "In stock", icon: "bi-check-circle" };
}

function createInventoryCard(record) {
    const column = document.createElement("div");
    const card = document.createElement("a");
    const visual = document.createElement("div");
    const icon = document.createElement("i");
    const body = document.createElement("div");
    const label = createTextElement("p", "inventory-card__label", "Inventory record");
    const title = createTextElement("h2", "inventory-card__title", `Product ${record.productId}`);
    const meta = document.createElement("div");
    const productId = createTextElement("span", "inventory-card__meta", `Product ID: ${record.productId}`);
    const inventoryId = createTextElement("span", "inventory-card__meta", `Inventory ID: ${record.id}`);
    const availability = getAvailability(record.quantity);
    const status = document.createElement("div");
    const statusIcon = document.createElement("i");
    const quantity = createTextElement("strong", "inventory-card__quantity", `${record.quantity}`);
    const quantityLabel = createTextElement("span", "inventory-card__quantity-label", "units available");

    column.className = "col";
    card.className = "inventory-card";
    card.href = `product-details.html?id=${encodeURIComponent(record.id)}`;
    card.setAttribute("aria-label", `View details for product ${record.productId}`);
    visual.className = "inventory-card__visual";
    icon.className = "bi bi-box-seam";
    icon.setAttribute("aria-hidden", "true");
    body.className = "inventory-card__body";
    meta.className = "inventory-card__meta-list";
    status.className = `availability ${availability.className}`;
    statusIcon.className = `bi ${availability.icon}`;
    statusIcon.setAttribute("aria-hidden", "true");
    status.setAttribute("aria-label", availability.label);

    visual.append(icon);
    card.append(visual);
    body.append(label, title);
    meta.append(productId, inventoryId);
    body.append(meta);
    status.append(statusIcon, document.createTextNode(availability.label));
    body.append(status);
    const quantityBlock = document.createElement("div");
    quantityBlock.className = "inventory-card__quantity-block";
    quantityBlock.append(quantity, quantityLabel);
    body.append(quantityBlock);
    card.append(body);
    column.append(card);
    return column;
}

function renderInventory(records) {
    elements.grid.replaceChildren(...records.map(createInventoryCard));
    setVisible(elements.grid, records.length > 0);
    setVisible(elements.noResults, records.length === 0 && inventoryRecords.length > 0);
    elements.resultsCount.textContent = `${records.length} ${records.length === 1 ? "record" : "records"}`;
}

function filterInventory() {
    const query = elements.search.value.trim().toLowerCase();
    const filteredRecords = inventoryRecords.filter((record) =>
        String(record.id).includes(query) || String(record.productId).includes(query)
    );
    renderInventory(filteredRecords);
}

function validateInventoryResponse(data) {
    if (data === null || data === undefined) return null;
    if (!Array.isArray(data)) throw new Error("The inventory response was not a list.");

    return data.map((record) => {
        if (!record || record.id === null || record.id === undefined || record.productId === null || record.productId === undefined || !Number.isInteger(record.quantity) || record.quantity < 0) {
            throw new Error("The inventory response had an unexpected shape.");
        }
        return { id: record.id, productId: record.productId, quantity: record.quantity };
    });
}

function showLoading() {
    setVisible(elements.loading, true);
    setVisible(elements.error, false);
    setVisible(elements.auth, false);
    setVisible(elements.empty, false);
    setVisible(elements.noResults, false);
    setVisible(elements.grid, false);
    elements.resultsCount.textContent = "Loading inventory...";
}

function showError(error) {
    setVisible(elements.loading, false);
    setVisible(elements.error, true);
    setVisible(elements.auth, false);
    setVisible(elements.empty, false);
    setVisible(elements.grid, false);
    elements.resultsCount.textContent = "Inventory unavailable";
    elements.errorMessage.textContent = error.message || "We could not load the inventory right now. Please try again.";
}

function showAuthenticationState() {
    setVisible(elements.loading, false);
    setVisible(elements.error, false);
    setVisible(elements.auth, true);
    setVisible(elements.empty, false);
    setVisible(elements.grid, false);
    elements.resultsCount.textContent = "Authentication required";
}

async function loadInventory() {
    showLoading();
    try {
        const response = validateInventoryResponse(await getInventory());
        if (response === null) {
            showError(new Error("The inventory service returned no response."));
            return;
        }
        inventoryRecords = response;
        setVisible(elements.loading, false);
        if (inventoryRecords.length === 0) {
            setVisible(elements.empty, true);
            elements.resultsCount.textContent = "0 records";
            return;
        }
        filterInventory();
    } catch (error) {
        if (error.status === 401 || error.status === 403) showAuthenticationState();
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
elements.search.addEventListener("input", filterInventory);
document.querySelector("#retryInventory").addEventListener("click", loadInventory);
loadInventory();
