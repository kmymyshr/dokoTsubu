package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;

public class LoginLogic {
    private final UserService users;

    public LoginLogic() {
        this(ApplicationServiceBridge.users());
    }

    public LoginLogic(UserService users) {
        this.users = users;
    }

    public User login(User user) {
        return users.authenticate(user.getName(), user.getPass());
    }
}
