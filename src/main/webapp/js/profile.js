(function () {
  /**
   * プロフィール画面のフォロー切り替え用スクリプト。
   *
   * Phase7ではプロフィール画面自体はまだJSPだが、フォロー操作は画面遷移なしで行う。
   * 将来React化するときに同じAPIへ置き換えやすいよう、DOM操作の範囲をこのファイルに閉じ込める。
   */
  const root = document.getElementById("profileFollow");
  if (!root) return;

  const button = document.getElementById("followButton");
  const message = document.getElementById("followMessage");
  const followersCount = document.getElementById("followersCount");
  const contextPath = document.body.dataset.contextPath || "";
  const csrfToken = document.body.dataset.csrfToken;
  const csrfHeader = document.body.dataset.csrfHeader || "X-CSRF-Token";
  const followeeId = Number(root.dataset.userId);
  let followed = root.dataset.following === "true";

  function updateButton() {
    button.textContent = followed ? "フォロー中" : "フォロー";
  }

  async function handleFollowClick() {
    if (followed && !window.confirm("フォローを解除しますか？")) {
      return;
    }

    button.disabled = true;
    message.textContent = "";

    try {
      const response = await fetch(`${contextPath}/FollowUser`, {
        method: "POST",
        credentials: "same-origin",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ followeeId })
      });

      if (!response.ok) {
        let errorMessage = "フォロー処理に失敗しました";
        try {
          const error = await response.json();
          errorMessage = error.message || errorMessage;
        } catch (_) {
          // HTMLエラーなどJSON以外が返った場合は、共通メッセージを使う。
        }
        throw new Error(errorMessage);
      }

      const result = await response.json();
      followed = Boolean(result.following);
      root.dataset.following = String(followed);
      updateButton();
      if (followersCount) {
        followersCount.textContent = String(Number(result.followers));
      }
      message.textContent = followed ? "フォローしました" : "フォローを解除しました";
    } catch (error) {
      message.textContent = error.message || "フォロー処理に失敗しました";
    } finally {
      button.disabled = false;
    }
  }

  updateButton();
  button.addEventListener("click", handleFollowClick);
}());
