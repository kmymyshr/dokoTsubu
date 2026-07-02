import java.util.List;

import dao.EmployeesDAO;
import model.Employee;


public class SelectEmployees {

	public static void main(String[] args) {
// EMPLOYEESテーブルの全レコードを取得
		EmployeesDAO empDAO = new EmployeesDAO();
		List<Employee> empList = empDAO.findAll();

		// 取得したレコードを表示
		for (Employee emp : empList) {
		System.out.println("ID: " + emp.getId());
		System.out.println("Name: " + emp.getName());
		System.out.println("Age: " + emp.getAge());
		}			
		}
		
}
