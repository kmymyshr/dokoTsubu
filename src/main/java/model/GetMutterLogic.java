package model;

import dao.MutterDAO;

/** IDを指定してつぶやきを1件取得する。 */
public class GetMutterLogic {
	public Mutter execute(int mutterId) {
		return new MutterDAO().findById(mutterId);
	}
}
