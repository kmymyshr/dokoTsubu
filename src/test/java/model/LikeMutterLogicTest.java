package model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import dao.LikeDAO;

class LikeMutterLogicTest {

    @Test
    void executeReturnsActualLikedStateAfterToggle() {
        try (MockedConstruction<LikeDAO> mocked = Mockito.mockConstruction(LikeDAO.class, (mock, context) -> {
            when(mock.toggleLike(1, 2)).thenReturn(true);
            when(mock.hasLiked(1, 2)).thenReturn(false);
        })) {
            LikeMutterLogic logic = new LikeMutterLogic();

            boolean liked = logic.execute(1, 2);

            assertFalse(liked);
            LikeDAO dao = mocked.constructed().get(0);
            verify(dao).toggleLike(1, 2);
            verify(dao).hasLiked(1, 2);
        }
    }
}
