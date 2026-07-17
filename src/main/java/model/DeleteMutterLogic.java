package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class DeleteMutterLogic {
    private final MutterService mutters;

    public DeleteMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public DeleteMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public boolean execute(int mutterId, int userId) {
        return mutters.delete(mutterId, userId);
    }
}
