package model;

import dao.MutterDAO;

public class PostMutterLogic {

	public boolean execute(Mutter mutter) {
		MutterDAO dao = new MutterDAO();
		return dao.create(mutter);
	}

}
