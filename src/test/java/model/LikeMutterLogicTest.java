package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dokotsubu.service.SocialService;
import org.junit.jupiter.api.Test;

class LikeMutterLogicTest {

    @Test
    void executeReturnsActualLikedStateAfterToggle() {
        SocialService social = mock(SocialService.class);
        when(social.toggleLike(1, 2)).thenReturn(false);

        LikeMutterLogic logic = new LikeMutterLogic(social);

        boolean liked = logic.execute(1, 2);

        assertFalse(liked);
        verify(social).toggleLike(1, 2);
    }
}
