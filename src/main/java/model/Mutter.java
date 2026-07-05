package model;
import java.io.Serializable;


public class Mutter implements Serializable {
	private int id; //つぶやきID
	private int user_Id; //ユーザーID
	private String userName; //ユーザー名
	private String text; //つぶやき
	private int version; //楽観的ロック用のバージョン

	//自動補完が出ない
	//つぶやき投稿用のコンストラクタ
	
public Mutter(int user_Id, String text) {
		this.user_Id = user_Id;
		this.text = text;
	}
//つぶやき表示用のコンストラクタ
public Mutter(int id, int user_Id, String userName, String text) {
	this.id= id;
	this.user_Id = user_Id;
	this.userName = userName;
	this.text = text;
}

public Mutter(int id, int user_Id, String userName, String text, int version) {
	this.id = id;
	this.user_Id = user_Id;
	this.userName = userName;
	this.text = text;
	this.version = version;
}

public Mutter(int user_Id, String userName, String text) {
	this.user_Id = user_Id;
	this.userName = userName;
	this.text = text;
}


public int getId() {	return id;	}
	public int getUserId() {	return user_Id;	}
	public String getUserName() {return userName;}
	public String getText() {return text;}
	public int getVersion() {return version;}

}
