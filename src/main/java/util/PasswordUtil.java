package util;
import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

	private static final int COST = 12;
	
	public static String hashPassword(String plainPassword) {
		return BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
	}
	
	public static boolean checkPassword(String plainPassword, String hashedPassword) {
		if (plainPassword == null || hashedPassword == null) {
			return false;
		}
		return BCrypt.checkpw(plainPassword, hashedPassword);
	}
}
