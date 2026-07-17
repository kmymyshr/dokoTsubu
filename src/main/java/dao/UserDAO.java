package dao;

import com.example.dokotsubu.persistence.DataAccessBridge;
import model.User;
import org.springframework.dao.DataAccessException;

/**
 * 既存呼出側を維持しながらSpring Data JDBCへ委譲する互換アダプター。
 */
public class UserDAO {

    public User findById(int id) {
        try {
            return DataAccessBridge.get().findUserById(id);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User findByName(String name) {
        try {
            return DataAccessBridge.get().findUserByName(name);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean create(User user) {
        try {
            return DataAccessBridge.get().createUser(user);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBio(int userId, String bio) {
        try {
            return DataAccessBridge.get().updateUserBio(userId, bio);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
