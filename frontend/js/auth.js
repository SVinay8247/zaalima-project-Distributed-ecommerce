const ACCESS_TOKEN_KEY = "zaalima_access_token";

export function getAccessToken() {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token) {
    if (!token || !token.trim()) {
        throw new Error("Access token is required.");
    }

    sessionStorage.setItem(ACCESS_TOKEN_KEY, token.trim());
}

export function clearAccessToken() {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
}

export function showAuthenticationRequired(showMessage) {
    showMessage("Authentication is required to access this feature.");
}
