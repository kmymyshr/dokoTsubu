(function () {
  /**
   * フォロー中/フォロワー一覧のフォローボタン制御。
   *
   * Phase8では一覧取得APIを追加したが、既存JSP画面ではこの小さなJSで非同期操作だけを担当する。
   * 将来React化するときは、ここで行っている「ボタン状態更新」「解除後の行削除」を
   * Reactのstate更新へ移せばよいように、DOM操作をこのファイルに閉じ込めている。
   */
  const buttons = Array.from(document.querySelectorAll(".follow-toggle"));
  if (buttons.length === 0) return;

  const contextPath = document.body.dataset.contextPath || "";
  const csrfToken = document.body.dataset.csrfToken;
  const csrfHeader = document.body.dataset.csrfHeader || "X-CSRF-Token";
  const listCount = document.getElementById("listCount");
  const emptyState = document.getElementById("emptyState");
  const followList = document.getElementById("followList");
  const message = document.getElementById("followActionMessage");

  /** ボタンのdata属性と表示文言を、切り替え後の状態にそろえる。 */
  function updateButton(button, followed) {
    button.dataset.following = String(followed);
    button.textContent = followed ? "フォロー済" : "フォロー";
  }

  /** 自分のフォロー中一覧で解除により行が0件になった場合、空表示を復活させる。 */
  function updateEmptyState() {
    if (!followList || !emptyState) return;
    const hasRows = followList.querySelector("li") !== null;
    emptyState.style.display = hasRows ? "none" : "";
  }

  /** FollowUser ServletへJSONでPOSTし、一覧画面の表示だけを最小限更新する。 */
  async function handleClick(event) {
    const button = event.currentTarget;
    const followeeId = Number(button.dataset.userId);
    const followed = button.dataset.following === "true";
    const removeOnUnfollow = button.dataset.removeOnUnfollow === "true";

    if (followed && !window.confirm("フォローを解除しますか？")) {
      return;
    }

    button.disabled = true;
    if (message) message.textContent = "";

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
          // ignore
        }
        throw new Error(errorMessage);
      }

      const result = await response.json();
      const nextFollowing = Boolean(result.following);

      if (!nextFollowing && removeOnUnfollow) {
        const row = button.closest("li");
        row?.remove();
        if (listCount) {
          const currentCount = Number(listCount.textContent || "0");
          listCount.textContent = String(Math.max(0, currentCount - 1));
        }
        updateEmptyState();
      } else {
        updateButton(button, nextFollowing);
      }

      if (message) {
        message.textContent = nextFollowing ? "フォローしました" : "フォローを解除しました";
      }
    } catch (error) {
      if (message) {
        message.textContent = error.message || "フォロー処理に失敗しました";
      }
    } finally {
      if (document.body.contains(button)) {
        button.disabled = false;
      }
    }
  }

  for (const button of buttons) {
    button.addEventListener("click", handleClick);
  }
}());
