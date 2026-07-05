//ユーザ情報がテーブルに存在するかチェックする(executeメソッドは不要のため削除）

//userテーブルにuserとpassが一致するデータがあればtrueを返すように変更
//UserDAO.javaによって、登録チェック

package model;

//bcrypt導入
import org.mindrot.jbcrypt.BCrypt;

import dao.UserDAO;

public class LoginLogic {
	//loginメソッドを実行してパスワードが一致すればdbUser(userのID,name,passが入っているもの)を返す
	public User login(User user) {
       UserDAO dao = new UserDAO();
        
       User dbUser = dao.findByName(user.getName());
		
		if (dbUser == null) {
			return null; // ユーザが存在しない場合
		}
		
		if (BCrypt.checkpw(user.getPass(), dbUser.getPass())) {
			return dbUser; // パスワードが一致する場合
		}
			return null; // パスワードが一致しない場合
		       
    }
}