import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {
 
	public static void main(String[] args) {
		// パスワードをハッシュ化
		String password = "password123";
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
		System.out.println("Hashed Password: " + hashedPassword);
		
		boolean isMatch1 = BCrypt.checkpw("password123", hashedPassword);
		boolean isMatch2 = BCrypt.checkpw("wrongpassword", hashedPassword);
		
		System.out.println("Password matches: " + isMatch1); // true
		System.out.println("Password matches: " + isMatch2); // false
	}
		
		
}
	
	
