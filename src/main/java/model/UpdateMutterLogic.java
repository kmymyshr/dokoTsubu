package model;

import dao.MutterDAO;

public class UpdateMutterLogic {
	/**
	 * つぶやき内容を更新する。
	 */
	public boolean execute(Mutter mutter) {
		return new MutterDAO().update(mutter);
	}
}
