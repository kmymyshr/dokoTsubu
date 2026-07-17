package model;

import com.example.dokotsubu.service.ApplicationServiceBridge;
import com.example.dokotsubu.service.SocialService;

public class LikeMutterLogic {
    private final SocialService social;

    public LikeMutterLogic() {
        this(ApplicationServiceBridge.social());
    }

    public LikeMutterLogic(SocialService social) {
        this.social = social;
    }

    public boolean execute(int mutterId, int userId) {
        return social.toggleLike(mutterId, userId);
    }

    public int countLikes(int mutterId) {
        return social.countLikes(mutterId);
    }

    public boolean hasLiked(int mutterId, int userId) {
        return social.hasLiked(mutterId, userId);
    }
}
