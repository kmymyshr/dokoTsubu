package model;

import dao.MutterDAO;

public class UpdateMutterLogic {
	public boolean execute(Mutter mutter) {
		return new MutterDAO().update(mutter);
	}
}
