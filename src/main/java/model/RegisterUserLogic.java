//ユーザ情報をデータベースに登録する。そういう処理で使う。
//Bcrypt予定

package model;

//bcrypt

import org.mindrot.jbcrypt.BCrypt;

import dao.UserDAO;

public class RegisterUserLogic {

	public boolean execute(User user) {

		String hashedPass = BCrypt.hashpw(user.getPass(), BCrypt.gensalt(12));

		User hashedUser = new User(user.getName(), hashedPass);

		UserDAO dao = new UserDAO();
		return dao.create(hashedUser);
	}
}