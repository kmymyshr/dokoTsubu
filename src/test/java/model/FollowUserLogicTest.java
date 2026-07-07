package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import dao.FollowDAO;

class FollowUserLogicTest {

    @Test
    void executeReturnsActualFollowingStateAfterToggle() {
        try (MockedConstruction<FollowDAO> mocked = Mockito.mockConstruction(FollowDAO.class, (mock, context) -> {
            when(mock.toggleFollow(1, 2)).thenReturn(true);
            when(mock.isFollowing(1, 2)).thenReturn(false);
        })) {
            FollowUserLogic logic = new FollowUserLogic();

            boolean following = logic.execute(1, 2);

            assertFalse(following);
            FollowDAO dao = mocked.constructed().get(0);
            verify(dao).toggleFollow(1, 2);
            verify(dao).isFollowing(1, 2);
        }
    }
}
