//ユーザ情報をデータベースに登録する。そういう処理で使う。

package model;

import dao.UserDAO;

public class RegisterUserLogic {

    public boolean execute(User user) {
        UserDAO dao = new UserDAO();
        return dao.create(user);
    }
}