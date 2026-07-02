package model;

import java.util.List;

import dao.MutterDAO;


public class PostMutterLogic {
	
	public void execute(Mutter mutter, List<Mutter> mutterList) {
MutterDAO dao = new MutterDAO();
boolean result = dao.create(mutter);
		if (result) {
					mutterList.add(0,mutter);
	}
}
}

