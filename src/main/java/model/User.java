package model;

import java.io.Serializable;

public class User implements Serializable {

	private int id;
	private String name;
	private String pass;
	private String bio;

	public User() {
	}

	public User(int id, String name, String pass) {
		this(id, name, pass, "");
	}

	public User(int id, String name, String pass, String bio) {
		this.id = id;
		this.name = name;
		this.pass = pass;
		this.bio = bio;
	}

	public User(String name, String pass) {
		this(name, pass, "");
	}

	public User(String name, String pass, String bio) {
		this.name = name;
		this.pass = pass;
		this.bio = bio;
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

	public String getBio() {
		return bio;
	}
}
