package model;

import dao.MutterDAO;

/**
 * ID を指定して、1 件のつぶやきを取得する。
 * 編集画面などで利用する。
 */
public class GetMutterLogic {
	public Mutter execute(int mutterId) {
		return new MutterDAO().findById(mutterId);
	}
}
