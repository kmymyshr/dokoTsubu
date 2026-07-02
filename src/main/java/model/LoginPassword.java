package model;

import java.io.Serializable;

public class LoginPassword implements Serializable {
	private String loginId;
	private String password;
	private String password_hash;

	public LoginPassword() {
	}
	public LoginPassword(String loginId, String password) {
		this.loginId = loginId;
		this.password = password;
	}

	public String getLoingId() {
		return loginId;
	}

	public void setLoginId(String LoginId) {
		this.loginId = loginId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword_hash() {
		return password_hash;
	}
	
	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}
	
	
	
}
