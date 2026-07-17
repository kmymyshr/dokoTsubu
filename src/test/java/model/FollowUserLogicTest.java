package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dokotsubu.service.SocialService;
import org.junit.jupiter.api.Test;

class FollowUserLogicTest {

    @Test
    void executeReturnsActualFollowingStateAfterToggle() {
        SocialService social = mock(SocialService.class);
        when(social.toggleFollow(1, 2)).thenReturn(false);

        FollowUserLogic logic = new FollowUserLogic(social);

        boolean following = logic.execute(1, 2);

        assertFalse(following);
        verify(social).toggleFollow(1, 2);
    }
}
