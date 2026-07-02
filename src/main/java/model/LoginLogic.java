//ユーザ情報がテーブルに存在するかチェックする(executeメソッドは不要のため削除）

//userテーブルにuserとpassが一致するデータがあればtrueを返すように変更
//UserDAO.javaによって、登録チェック

package model;

import dao.UserDAO;

public class LoginLogic {

    public User login(User user) {
        UserDAO dao = new UserDAO();
        return dao.findByLogin(user);
    }
}