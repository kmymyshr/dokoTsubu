package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Employee;


public class EmployeesDAO {

private final String JDBC_URL = "jdbc:h2:tcp://localhost/~/example";
private final String DB_USER = "sa";
private final String DB_PASS = "";

public List<Employee> findAll() {
	List<Employee> empList = new ArrayList<>();

	// JDBCドライバの読み込み
	try {
		Class.forName("org.h2.Driver");
	} catch (ClassNotFoundException e) {
		throw new IllegalStateException("JDBCドライバを読み込めませんでした");
		
	}
	
	// データベースへの接続	
	
	try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS)) {
		
		// SQL文の準備
		
		String sql = "SELECT id, name, age FROM employees";
		PreparedStatement pStmt = conn.prepareStatement(sql);

		ResultSet rs = pStmt.executeQuery();

		//結果表に格納されたレコードの内容を
		// Employeeインスタンスに設定し、ArrayListに追加する
		
		
		while (rs.next()) {
			String id = rs.getString("id");
			String name = rs.getString("name");
			int age = rs.getInt("age");

			Employee employee = new Employee(id, name, age);
			empList.add(employee);
		}
	} catch (SQLException e) {
		e.printStackTrace();
		return null;
	}

	return empList;

}
}

