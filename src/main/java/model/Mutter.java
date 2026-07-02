package model;
import java.io.Serializable;


public class Mutter implements Serializable {
	private int id; //つぶやきID
	private int user_Id; //ユーザーID
	private String userName; //ユーザー名
	private String text; //つぶやき

	
	//つぶやき表示用のコンストラクタ
	
	public Mutter(int id, String userName, String text) {
		this.id= id;
		this.userName = userName;
		this.text = text;
	}
	
	
	//つぶやき投稿用のコンストラクタ
	
public Mutter(int user_Id, String text) {
		this.user_Id = user_Id;
		this.text = text;
	}
	
	public int getUserId() {	return user_Id;	}
	public String getUserName() {return userName;}
	public String getText() {return text;}

}
