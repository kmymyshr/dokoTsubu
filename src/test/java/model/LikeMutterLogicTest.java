package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dokotsubu.service.SocialService;
import org.junit.jupiter.api.Test;

/**
 * LikeMutterLogicがSocialServiceへの薄い委譲層として動くことを確認するテスト。
 *
 * <p>Phase5で実処理をServiceへ移したため、ここではDBを使わずServiceをモックして
 * 旧Logicの戻り値仕様が維持されているかを検証する。</p>
 */
class LikeMutterLogicTest {

    @Test
    void executeReturnsActualLikedStateAfterToggle() {
        // toggle後の実状態をそのまま返す仕様を固定する。
        SocialService social = mock(SocialService.class);
        when(social.toggleLike(1, 2)).thenReturn(false);

        LikeMutterLogic logic = new LikeMutterLogic(social);

        boolean liked = logic.execute(1, 2);

        assertFalse(liked);
        verify(social).toggleLike(1, 2);
    }
}
