import { getAccessToken } from "./auth.js";

const API_BASE_URL = "http://localhost:8090";

export function getApiBaseUrl() {
    return API_BASE_URL;
}

export function getInventory() {
    return request("/inventory");
}

export function getInventoryById(id) {
    return request(`/inventory/${encodeURIComponent(id)}`);
}

export function createOrder(productId, quantity) {
    return request("/orders", {
        method: "POST",
        body: JSON.stringify({ productId, quantity })
    });
}

export function getOrders() {
    return request("/orders");
}

export function getOrderById(id) {
    return request(`/orders/${encodeURIComponent(id)}`);
}

export async function request(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();

    const headers = {
        Accept: "application/json",
        ...(options.headers || {})
    };

    const accessToken = getAccessToken();

    if (accessToken) {
        headers.Authorization = `Bearer ${accessToken}`;
    }

    if (method === "GET") {
        delete headers["Content-Type"];
    } else if (
        options.body !== undefined &&
        options.body !== null &&
        !headers["Content-Type"]
    ) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers
    });

    if (!response.ok) {
        const error = new Error(
            `API request failed with status ${response.status}`
        );

        error.status = response.status;
        throw error;
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();

    return text ? JSON.parse(text) : null;
}
