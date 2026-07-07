"use strict";

import {
    createMutter,
    deleteMutter,
    fetchMutterPage,
    fetchSession,
    updateMutter
} from "./api.js";
import {
    clearKeyword,
    clearMessage,
    clearPostText,
    closeEditDialog,
    elements,
    getEditValues,
    openEditDialog,
    renderMutters,
    setLoading,
    setLoadMoreVisible,
    showLoginUser,
    showMessage
} from "./ui.js";

const PAGE_LIMIT = 20;
const state = {
    loginUser: null,
    keyword: "",
    nextCursor: null,
    hasNext: false,
    olderPagesLoaded: false,
    loading: false
};

async function loadMutters({ append = false, silent = false } = {}) {
    if (state.loading) return;
    state.loading = true;
    setLoading(true);
    try {
        const page = await fetchMutterPage({
            keyword: state.keyword,
            cursor: append ? state.nextCursor : null,
            limit: PAGE_LIMIT
        });
        renderMutters(page.mutters, {
            append,
            loginUser: state.loginUser,
            onEdit: openEditDialog,
            onDelete: handleDelete
        });
        state.nextCursor = page.nextCursor;
        state.hasNext = page.hasNext;
        if (append) state.olderPagesLoaded = true;
        setLoadMoreVisible(state.hasNext);
        if (!silent) clearMessage();
    } catch (error) {
        if (!silent) showMessage(error.message, true);
        console.error(error);
    } finally {
        state.loading = false;
        setLoading(false);
    }
}

async function handleDelete(mutter) {
    if (!window.confirm("このつぶやきを削除しますか？")) return;
    try {
        await deleteMutter(mutter.id);
        showMessage("つぶやきを削除しました。");
        state.olderPagesLoaded = false;
        await loadMutters({ silent: true });
    } catch (error) {
        showMessage(error.message, true);
    }
}

function resetToLatest() {
    state.keyword = "";
    state.olderPagesLoaded = false;
    clearKeyword();
}

async function initialize() {
    try {
        state.loginUser = await fetchSession();
        showLoginUser(state.loginUser);
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
        clearPostText();
        resetToLatest();
        showMessage("つぶやきを投稿しました。");
        await loadMutters({ silent: true });
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
    resetToLatest();
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
    const edit = getEditValues();
    if (!edit.text) return showMessage("つぶやきを入力してください。", true);
    try {
        await updateMutter(edit.id, edit.text, edit.version);
        closeEditDialog();
        showMessage("つぶやきを更新しました。");
        state.olderPagesLoaded = false;
        await loadMutters({ silent: true });
    } catch (error) {
        showMessage(error.message, true);
    }
});

elements.cancelEditButton.addEventListener("click", closeEditDialog);

setInterval(() => {
    if (!state.keyword && !state.olderPagesLoaded && !elements.editDialog.open) {
        loadMutters({ silent: true });
    }
}, 5000);

initialize();
