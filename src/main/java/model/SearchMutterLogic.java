package model;
import java.util.List;

import dao.MutterDAO;

public class SearchMutterLogic {

	//MutterDAOのsearchメソッドを呼び出し、検索結果を返す
	public List<Mutter> execute(String keyword) {
		MutterDAO dao = new MutterDAO();
		return dao.search(keyword);
	}
	
	
	
	
}
