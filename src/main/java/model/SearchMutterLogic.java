package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class SearchMutterLogic {
    private final MutterService mutters;

    public SearchMutterLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public SearchMutterLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public List<Mutter> execute(String keyword) {
        return mutters.search(keyword);
    }
}
