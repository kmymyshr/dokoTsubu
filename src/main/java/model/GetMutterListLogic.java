package model;

import java.util.List;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.MutterService;

public class GetMutterListLogic {
    public static final int DEFAULT_LIMIT = MutterService.DEFAULT_LIMIT;

    private final MutterService mutters;

    public GetMutterListLogic() {
        this(ApplicationServiceBridge.mutters());
    }

    public GetMutterListLogic(MutterService mutters) {
        this.mutters = mutters;
    }

    public List<Mutter> execute() {
        return mutters.findAll();
    }

    public MutterPage execute(String keyword, Integer cursor, int limit) {
        return mutters.findPage(keyword, cursor, limit);
    }

    public MutterFeedPage executeFeed(String keyword, Integer cursor, int limit, int viewerId) {
        return mutters.findFeedPage(keyword, cursor, limit, viewerId);
    }

    public MutterPage execute(Integer cursor, int limit) {
        return mutters.findTimelinePage(cursor, limit);
    }
}
