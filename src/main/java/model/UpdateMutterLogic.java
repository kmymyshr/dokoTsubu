package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class UpdateMutterLogic {
    private final MutterService mutters;

    public UpdateMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public UpdateMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public boolean execute(Mutter mutter) {
        return mutters.update(mutter);
    }
}
