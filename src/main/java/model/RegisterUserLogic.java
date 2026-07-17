package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.UserService;

public class RegisterUserLogic {
    private final UserService users;

    public RegisterUserLogic() {
        this(ApplicationServiceBridge.users());
    }

    public RegisterUserLogic(UserService users) {
        this.users = users;
    }

    public boolean execute(User user) {
        return users.register(user);
    }
}
