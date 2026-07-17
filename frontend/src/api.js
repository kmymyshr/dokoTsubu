/**
 * React画面からバックエンドAPIを呼び出すための薄いクライアント。
 *
 * Phase6ではメイン画面の投稿一覧・投稿作成・検索・編集・削除をReactへ寄せるため、
 * fetch処理、CSRFヘッダー付与、共通エラー処理をこのファイルへ集約している。
 */
const protectedMethods = new Set(["POST", "PUT", "PATCH", "DELETE"]);
let contextPath = "";
let csrfToken = null;

/** JSPホストから渡されたcontextPathを保持し、どの環境でも同じAPIパスを組み立てる。 */
export function configureApi(path) {
  contextPath = path;
}

/**
 * JSPホストから渡されたCSRFトークンを保持する。
 *
 * ログイン後画面では `/api/session` から取得するが、未ログインで使う登録画面では
 * registerView.jsp のdata属性から受け取ったトークンを使う。
 */
export function configureCsrfToken(token) {
  csrfToken = token || null;
}

/** fetchの共通処理。CSRF付与、401時のログイン画面遷移、JSONエラーの取り出しを担う。 */
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
      // HTMLエラーなどJSON以外が返った場合は、共通メッセージをそのまま使う。
    }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

/** 初期表示時にログインユーザーとCSRFトークンを取得する。 */
export async function fetchSession() {
  const session = await request("/api/session");
  csrfToken = session.csrfToken;
  return { id: session.id, name: session.name, csrfToken: session.csrfToken };
}

/** 投稿一覧を取得する。keyword/cursor/limitはReact側の検索・追加読み込みで使う。 */
export function fetchMutterPage({ keyword = "", cursor = null, limit = 20 }) {
  const query = new URLSearchParams({ limit });
  if (keyword) query.set("keyword", keyword);
  if (cursor !== null) query.set("cursor", cursor);
  return request(`/api/mutters?${query}`);
}

/** 新しい投稿を作成する。 */
export function createMutter(text) {
  return request("/api/mutters", {
    method: "POST",
    body: JSON.stringify({ text })
  });
}

/** 既存投稿を更新する。versionは楽観ロックのために送る。 */
export function updateMutter(id, text, version) {
  return request(`/api/mutters/${id}`, {
    method: "PUT",
    body: JSON.stringify({ text, version })
  });
}

/** 既存投稿を削除する。 */
export function deleteMutter(id) {
  return request(`/api/mutters/${id}`, { method: "DELETE" });
}

/** 投稿のいいね状態を切り替える。 */
export function likeMutter(mutterId) {
  return request(`/LikeMutter`, {
    method: "POST",
    body: JSON.stringify({ mutterId })
  });
}

/** 投稿者へのフォロー状態を切り替える。 */
export function followUser(followeeId) {
  return request(`/FollowUser`, {
    method: "POST",
    body: JSON.stringify({ followeeId })
  });
}

/**
 * フォロー中/フォロワー一覧を取得する。
 *
 * Phase9ではJSPが持っていた一覧描画をReactへ移すため、Phase8で追加した
 * `/api/follows` をReact側の正式なデータ取得口として使う。
 */
export function fetchFollowList({ userId, type }) {
  const query = new URLSearchParams({ userId, type });
  return request(`/api/follows?${query}`);
}

/** 新しいユーザーを登録する。未ログイン画面から呼ぶため、JSPホスト由来のCSRFトークンを使う。 */
export function registerUser({ name, pass }) {
  return request("/api/register", {
    method: "POST",
    body: JSON.stringify({ name, pass })
  });
}
