package model;

import dao.MutterDAO;

public class DeleteMutterLogic {
	/**
	 * 指定されたつぶやきを、ログイン中ユーザーのものとして削除する。
	 */
	public boolean execute(int mutterId, int userId) {
		return new MutterDAO().delete(mutterId, userId);
	}
}
