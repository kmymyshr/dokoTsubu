"use strict";

export const elements = {
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

export function showLoginUser(user) {
    elements.loginStatus.textContent = `${user.id} ${user.name} さん、ログイン中です`;
}

export function showMessage(text, isError = false) {
    elements.message.textContent = text;
    elements.message.classList.toggle("error", isError);
    elements.message.hidden = false;
}

export function clearMessage() {
    elements.message.hidden = true;
    elements.message.textContent = "";
}

export function setLoading(loading) {
    elements.mutterList.setAttribute("aria-busy", String(loading));
    elements.loadMoreButton.disabled = loading;
}

export function setLoadMoreVisible(visible) {
    elements.loadMoreButton.hidden = !visible;
}

export function clearPostText() {
    elements.postText.value = "";
}

export function clearKeyword() {
    elements.keyword.value = "";
}

export function openEditDialog(mutter) {
    elements.editId.value = mutter.id;
    elements.editVersion.value = mutter.version;
    elements.editText.value = mutter.text;
    elements.editDialog.showModal();
    elements.editText.focus();
}

export function closeEditDialog() {
    elements.editDialog.close();
}

export function getEditValues() {
    return {
        id: Number(elements.editId.value),
        version: Number(elements.editVersion.value),
        text: elements.editText.value.trim()
    };
}

export function renderMutters(mutters, { append, loginUser, onEdit, onDelete }) {
    if (!append) elements.mutterList.replaceChildren();
    const fragment = document.createDocumentFragment();
    mutters.forEach(mutter => fragment.appendChild(
            createMutterElement(mutter, loginUser, onEdit, onDelete)));
    elements.mutterList.appendChild(fragment);
    elements.emptyMessage.hidden = elements.mutterList.childElementCount > 0;
}

function createMutterElement(mutter, loginUser, onEdit, onDelete) {
    const article = document.createElement("article");
    article.className = "mutter-card";
    article.dataset.mutterId = mutter.id;

    const meta = document.createElement("div");
    meta.className = "mutter-meta";
    const author = document.createElement("strong");
    author.textContent = mutter.userName;
    const time = document.createElement("time");
    time.dateTime = mutter.createdAt;
    time.textContent = formatDate(mutter.createdAt);
    meta.append(author, time);

    const text = document.createElement("p");
    text.className = "mutter-text";
    text.textContent = mutter.text;
    article.append(meta, text);

    if (loginUser && mutter.userId === loginUser.id) {
        const actions = document.createElement("div");
        actions.className = "mutter-actions";
        actions.append(
                createButton("編集", () => onEdit(mutter)),
                createButton("削除", () => onDelete(mutter)));
        article.appendChild(actions);
    }
    return article;
}

function createButton(label, listener) {
    const button = document.createElement("button");
    button.type = "button";
    button.textContent = label;
    button.addEventListener("click", listener);
    return button;
}

function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "" : date.toLocaleString("ja-JP");
}
