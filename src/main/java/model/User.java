//Usersテーブルのカラムとフィールドを一致するよう変更し、コンストラクタも追加
package model;
import java.io.Serializable;



public class User implements Serializable {
	
	private int id; //ユーザーID追加
	private String name; //ユーザー名
	private String pass; //パスワード

	//引数なしコンストラクタ
	
	public User() {
	}
	
	//DBから取得したユーザ情報を格納するためのコンストラクタ(IDあり)
	
	public User(int id, String name, String pass) {
		this.id = id;
		this.name = name;
		this.pass = pass;
	}
	
	//新規登録用(IDなし)
	
	public User(String name, String pass) {
		this.name = name;
		this.pass = pass;
	}
	

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

		public String getPass() {
		return pass;
	}
}
