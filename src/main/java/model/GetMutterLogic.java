package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class GetMutterLogic {
    private final MutterService mutters;

    public GetMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public GetMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public Mutter execute(int mutterId) {
        return mutters.findById(mutterId);
    }
}
