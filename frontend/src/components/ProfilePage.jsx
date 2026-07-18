/**
 * プロフィール画面を表示するReactコンポーネント。
 *
 * Phase11で旧JSPのプロフィール描画を置き換え、表示データ取得、自己紹介編集、
 * フォロー切り替え後の画面更新をReactへ集約する。バックエンドの業務ルールは
 * `/api/profile` とPhase20で追加した `/api/users/{id}/follow` に委譲する。
 */
import { useEffect, useState } from "react";
import { fetchProfile, followUser, updateProfileBio } from "../api.js";
import Header from "./Header.jsx";

/** プロフィール本文が未設定の場合に、画面上の空状態として表示する。 */
function bioText(profile) {
  return profile?.user?.bio ? profile.user.bio : "まだ自己紹介はありません。";
}

/**
 * API取得済みのプロフィール情報をHTMLへ変換する表示専用部分。
 * データ取得と分けることで、React化後の見た目をSSRテストで固定しやすくする。
 */
export function ProfileView({
  contextPath,
  sessionUser,
  profile,
  bio,
  loading,
  saving,
  message,
  onBioChange,
  onSave,
  onFollowClick
}) {
  return (
    <>
      <Header user={sessionUser} contextPath={contextPath} />
      <main>
        <p><a href={`${contextPath}/Main`}>Mainへ戻る</a></p>

        {message && <p className={`message${message.error ? " error" : ""}`} role="status">{message.text}</p>}
        {loading && <p>プロフィールを読み込み中です...</p>}

        {profile && (
          <section className="profile-card" aria-labelledby="profileHeading">
            <h1 id="profileHeading">{profile.user.name}</h1>
            <p>ユーザーID: {profile.user.id}</p>

            <div className="profile-stats">
              <p>
                フォロワー:{" "}
                <a href={`${contextPath}/FollowerList?userId=${profile.user.id}`}>{profile.followers}</a>
              </p>
              <p>
                フォロー中:{" "}
                <a href={`${contextPath}/FollowingList?userId=${profile.user.id}`}>{profile.followingCount}</a>
              </p>
            </div>

            <section className="profile-section">
              <h2>ひとこと自己紹介</h2>
              <p>{bioText(profile)}</p>
            </section>

            {profile.ownProfile ? (
              <section className="profile-section">
                <h2>自己紹介を編集</h2>
                <form onSubmit={onSave}>
                  <label htmlFor="profileBio">自己紹介</label>
                  <textarea
                    id="profileBio"
                    value={bio}
                    rows="4"
                    maxLength="160"
                    onChange={event => onBioChange(event.target.value)}
                  />
                  <div className="form-actions">
                    <button type="submit" disabled={saving || !sessionUser}>
                      {saving ? "保存中..." : "保存"}
                    </button>
                  </div>
                </form>
              </section>
            ) : (
              <button type="button" onClick={onFollowClick} disabled={!sessionUser}>
                {profile.following ? "フォロー中" : "フォロー"}
              </button>
            )}
          </section>
        )}
      </main>
      <footer><p>つぶやきアプリ</p></footer>
    </>
  );
}

export default function ProfilePage({ contextPath, sessionUser, userId }) {
  const [profile, setProfile] = useState(null);
  const [bio, setBio] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);

  /**
   * JSP時代にServletが詰めていたプロフィール属性をAPIから取得する。
   * 取得後は編集フォームの初期値も、保存済みの自己紹介にそろえる。
   */
  useEffect(() => {
    let active = true;

    async function loadProfile() {
      setLoading(true);
      try {
        const result = await fetchProfile(userId);
        if (!active) return;
        setProfile(result);
        setBio(result.user.bio || "");
        setMessage(null);
      } catch (error) {
        if (active) setMessage({ text: error.message, error: true });
      } finally {
        if (active) setLoading(false);
      }
    }

    loadProfile();
    return () => {
      active = false;
    };
  }, [userId]);

  /** 本人プロフィールの自己紹介を保存し、成功時はAPIレスポンスで画面状態を更新する。 */
  async function handleSave(event) {
    event.preventDefault();
    setSaving(true);
    setMessage(null);

    try {
      const result = await updateProfileBio({ userId, bio });
      setProfile(result);
      setBio(result.user.bio || "");
      setMessage({ text: "自己紹介を更新しました。", error: false });
    } catch (error) {
      setMessage({ text: error.message || "自己紹介の更新に失敗しました。", error: true });
    } finally {
      setSaving(false);
    }
  }

  /**
   * 他ユーザーのフォロー状態を切り替える。
   * 既存APIが返すfollowersを使い、ボタン状態とフォロワー数だけを局所更新する。
   */
  async function handleFollowClick() {
    if (profile.following && !window.confirm("フォローを解除しますか？")) {
      return;
    }

    try {
      const result = await followUser(profile.user.id);
      setProfile(current => ({
        ...current,
        following: Boolean(result.following),
        followers: Number(result.followers)
      }));
      setMessage({
        text: result.following ? "フォローしました。" : "フォローを解除しました。",
        error: false
      });
    } catch (error) {
      setMessage({ text: error.message || "フォロー処理に失敗しました。", error: true });
    }
  }

  return (
    <ProfileView
      contextPath={contextPath}
      sessionUser={sessionUser}
      profile={profile}
      bio={bio}
      loading={loading}
      saving={saving}
      message={message}
      onBioChange={setBio}
      onSave={handleSave}
      onFollowClick={handleFollowClick}
    />
  );
}
