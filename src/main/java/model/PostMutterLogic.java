package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class PostMutterLogic {
    private final MutterService mutters;

    public PostMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public PostMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public boolean execute(Mutter mutter) {
        return mutters.create(mutter);
    }
}
