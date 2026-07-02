package model;
import java.io.Serializable;


public class Mutter implements Serializable {

	private int user_Id; //ユーザーID
	private String userName; //ユーザー名
	private String text; //つぶやき

public Mutter(int user_Id, String userName, String text) {
		this.user_Id = user_Id;
		this.userName = userName;
		this.text = text;
	}
	
	public int getUserId() {
		return user_Id;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getText() {
		return text;
	}

}
