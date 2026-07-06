"use strict";

const contextPath = document.body.dataset.contextPath;
const pageLimit = 20;
const state = {
    loginUser: null,
    keyword: "",
    nextCursor: null,
    hasNext: false,
    olderPagesLoaded: false,
    loading: false
};

const elements = {
    loginStatus: document.getElementById("loginStatus"),
    message: document.getElementById("message"),
    postForm: document.getElementById("postForm"),
    postText: document.getElementById("postText"),
    searchForm: document.getElementById("searchForm"),
    keyword: document.getElementById("keyword"),
    clearSearchButton: document.getElementById("clearSearchButton"),
    refreshButton: document.getElementById("refreshButton"),
    mutterList: document.getElementById("mutterList"),
    emptyMessage: document.getElementById("emptyMessage"),
    loadMoreButton: document.getElementById("loadMoreButton"),
    editDialog: document.getElementById("editDialog"),
    editForm: document.getElementById("editForm"),
    editId: document.getElementById("editId"),
    editVersion: document.getElementById("editVersion"),
    editText: document.getElementById("editText"),
    cancelEditButton: document.getElementById("cancelEditButton")
};

function apiUrl(path) {
    return `${contextPath}${path}`;
}

async function apiFetch(path, options = {}) {
    const response = await fetch(apiUrl(path), {
        credentials: "same-origin",
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.body ? { "Content-Type": "application/json" } : {}),
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
            // JSON以外のエラーでも共通メッセージを表示する。
        }
        throw new Error(message);
    }
    return response.status === 204 ? null : response.json();
}

function showMessage(text, isError = false) {
    elements.message.textContent = text;
    elements.message.classList.toggle("error", isError);
    elements.message.hidden = false;
}

function clearMessage() {
    elements.message.hidden = true;
    elements.message.textContent = "";
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "" : date.toLocaleString("ja-JP");
}

function createMutterElement(mutter) {
    const article = document.createElement("article");
    article.className = "mutter-card";
    article.dataset.mutterId = mutter.id;

    const heading = document.createElement("div");
    heading.className = "mutter-meta";
    const author = document.createElement("strong");
    author.textContent = mutter.userName;
    const time = document.createElement("time");
    time.dateTime = mutter.createdAt;
    time.textContent = formatDate(mutter.createdAt);
    heading.append(author, time);

    const text = document.createElement("p");
    text.className = "mutter-text";
    text.textContent = mutter.text;
    article.append(heading, text);

    if (state.loginUser && mutter.userId === state.loginUser.id) {
        const actions = document.createElement("div");
        actions.className = "mutter-actions";
        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.textContent = "編集";
        editButton.addEventListener("click", () => openEditDialog(mutter));
        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.textContent = "削除";
        deleteButton.addEventListener("click", () => deleteMutter(mutter));
        actions.append(editButton, deleteButton);
        article.appendChild(actions);
    }
    return article;
}

function renderMutters(mutters, append) {
    if (!append) {
        elements.mutterList.replaceChildren();
    }
    const fragment = document.createDocumentFragment();
    mutters.forEach(mutter => fragment.appendChild(createMutterElement(mutter)));
    elements.mutterList.appendChild(fragment);
    elements.emptyMessage.hidden = elements.mutterList.childElementCount > 0;
}

async function fetchMutterPage(cursor = null) {
    const query = new URLSearchParams({ limit: pageLimit });
    if (state.keyword) query.set("keyword", state.keyword);
    if (cursor !== null) query.set("cursor", cursor);
    return apiFetch(`/api/mutters?${query}`);
}

async function loadMutters({ append = false, silent = false } = {}) {
    if (state.loading) return;
    state.loading = true;
    elements.mutterList.setAttribute("aria-busy", "true");
    elements.loadMoreButton.disabled = true;
    try {
        const page = await fetchMutterPage(append ? state.nextCursor : null);
        renderMutters(page.mutters, append);
        state.nextCursor = page.nextCursor;
        state.hasNext = page.hasNext;
        if (append) state.olderPagesLoaded = true;
        elements.loadMoreButton.hidden = !state.hasNext;
        if (!silent) clearMessage();
    } catch (error) {
        if (!silent) showMessage(error.message, true);
        console.error(error);
    } finally {
        state.loading = false;
        elements.mutterList.setAttribute("aria-busy", "false");
        elements.loadMoreButton.disabled = false;
    }
}

async function createMutter(text) {
    await apiFetch("/api/mutters", {
        method: "POST",
        body: JSON.stringify({ text })
    });
    elements.postText.value = "";
    state.keyword = "";
    elements.keyword.value = "";
    state.olderPagesLoaded = false;
    showMessage("つぶやきを投稿しました。");
    await loadMutters({ silent: true });
}

function openEditDialog(mutter) {
    elements.editId.value = mutter.id;
    elements.editVersion.value = mutter.version;
    elements.editText.value = mutter.text;
    elements.editDialog.showModal();
    elements.editText.focus();
}

async function updateMutter() {
    const id = elements.editId.value;
    await apiFetch(`/api/mutters/${id}`, {
        method: "PUT",
        body: JSON.stringify({
            text: elements.editText.value,
            version: Number(elements.editVersion.value)
        })
    });
    elements.editDialog.close();
    showMessage("つぶやきを更新しました。");
    state.olderPagesLoaded = false;
    await loadMutters({ silent: true });
}

async function deleteMutter(mutter) {
    if (!window.confirm("このつぶやきを削除しますか？")) return;
    try {
        await apiFetch(`/api/mutters/${mutter.id}`, { method: "DELETE" });
        showMessage("つぶやきを削除しました。");
        state.olderPagesLoaded = false;
        await loadMutters({ silent: true });
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function initialize() {
    try {
        state.loginUser = await apiFetch("/api/session");
        elements.loginStatus.textContent = `${state.loginUser.id} ${state.loginUser.name} さん、ログイン中です`;
        await loadMutters();
    } catch (error) {
        showMessage(error.message, true);
    }
}

elements.postForm.addEventListener("submit", async event => {
    event.preventDefault();
    const text = elements.postText.value.trim();
    if (!text) return showMessage("つぶやきを入力してください。", true);
    try {
        await createMutter(text);
    } catch (error) {
        showMessage(error.message, true);
    }
});

elements.searchForm.addEventListener("submit", async event => {
    event.preventDefault();
    state.keyword = elements.keyword.value.trim();
    state.olderPagesLoaded = false;
    await loadMutters();
});

elements.clearSearchButton.addEventListener("click", async () => {
    elements.keyword.value = "";
    state.keyword = "";
    state.olderPagesLoaded = false;
    await loadMutters();
});

elements.refreshButton.addEventListener("click", () => {
    state.olderPagesLoaded = false;
    loadMutters();
});

elements.loadMoreButton.addEventListener("click", () => {
    if (state.hasNext && state.nextCursor !== null) loadMutters({ append: true });
});

elements.editForm.addEventListener("submit", async event => {
    event.preventDefault();
    if (!elements.editText.value.trim()) return;
    try {
        await updateMutter();
    } catch (error) {
        showMessage(error.message, true);
    }
});

elements.cancelEditButton.addEventListener("click", () => elements.editDialog.close());

setInterval(() => {
    if (!state.keyword && !state.olderPagesLoaded && !elements.editDialog.open) {
        loadMutters({ silent: true });
    }
}, 5000);

initialize();
