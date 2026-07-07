"use strict";

const contextPath = document.body.dataset.contextPath;
const protectedMethods = new Set(["POST", "PUT", "PATCH", "DELETE"]);
let csrfToken = null;

async function request(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    if (protectedMethods.has(method) && !csrfToken) {
        throw new Error("CSRFトークンを取得できません。画面を再読み込みしてください");
    }

    const response = await fetch(`${contextPath}${path}`, {
        credentials: "same-origin",
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...(protectedMethods.has(method) ? { "X-CSRF-Token": csrfToken } : {}),
            ...options.headers
        }
    });

    if (response.status === 401) {
        window.location.href = `${contextPath}/`;
        throw new Error("ログインが必要です");
    }
    if (!response.ok) {
        let message = `通信に失敗しました（${response.status}）`;
        try {
            const error = await response.json();
            message = error.message || message;
        } catch (_) {
            // JSON以外のエラーでは共通メッセージを利用する。
        }
        throw new Error(message);
    }
    return response.status === 204 ? null : response.json();
}

export async function fetchSession() {
    const session = await request("/api/session");
    csrfToken = session.csrfToken;
    return { id: session.id, name: session.name };
}

export function fetchMutterPage({ keyword = "", cursor = null, limit = 20 } = {}) {
    const query = new URLSearchParams({ limit });
    if (keyword) query.set("keyword", keyword);
    if (cursor !== null) query.set("cursor", cursor);
    return request(`/api/mutters?${query}`);
}

export function createMutter(text) {
    return request("/api/mutters", {
        method: "POST",
        body: JSON.stringify({ text })
    });
}

export function updateMutter(id, text, version) {
    return request(`/api/mutters/${id}`, {
        method: "PUT",
        body: JSON.stringify({ text, version })
    });
}

export function deleteMutter(id) {
    return request(`/api/mutters/${id}`, { method: "DELETE" });
}
