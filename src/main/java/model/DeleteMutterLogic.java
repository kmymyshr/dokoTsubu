package model;

import dao.MutterDAO;

public class DeleteMutterLogic {
	public boolean execute(int mutterId, int userId) {
		return new MutterDAO().delete(mutterId, userId);
	}
}
