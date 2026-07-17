package com.example.dokotsubu.service;

import java.util.ArrayList;
import java.util.List;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.Mutter;
import model.MutterFeedItem;
import model.MutterFeedPage;
import model.MutterPage;
import org.springframework.stereotype.Service;

@Service
public class MutterService {
    public static final int DEFAULT_LIMIT = 20;

    private final SpringDataJdbcGateway gateway;

    public MutterService(SpringDataJdbcGateway gateway) {
        this.gateway = gateway;
    }

    public List<Mutter> findLatest(int limit) {
        return gateway.findLatest(limit);
    }

    public List<Mutter> findByCursor(int cursor, int limit) {
        return gateway.findByCursor(cursor, limit);
    }

    public MutterPage findPage(String keyword, Integer cursor, int limit) {
        List<Mutter> fetched = gateway.findMutterPage(keyword, cursor, limit + 1);
        return toMutterPage(fetched, limit);
    }

    public MutterFeedPage findFeedPage(String keyword, Integer cursor, int limit, int viewerId) {
        List<MutterFeedItem> fetched = gateway.findFeedPage(keyword, cursor, limit + 1, viewerId);
        boolean hasNext = fetched.size() > limit;
        List<MutterFeedItem> items = new ArrayList<>(
                fetched.subList(0, Math.min(limit, fetched.size())));
        Integer nextCursor = hasNext && !items.isEmpty()
                ? items.get(items.size() - 1).mutter().getId() : null;
        return new MutterFeedPage(items, nextCursor, hasNext);
    }

    public MutterPage findTimelinePage(Integer cursor, int limit) {
        List<Mutter> fetched = cursor == null
                ? gateway.findLatest(limit + 1)
                : gateway.findByCursor(cursor, limit + 1);
        if (fetched == null) {
            fetched = new ArrayList<>();
        }
        return toMutterPage(fetched, limit);
    }

    public List<Mutter> findAll() {
        return findTimelinePage(null, DEFAULT_LIMIT).getMutters();
    }

    public Mutter findById(int mutterId) {
        return gateway.findMutterById(mutterId);
    }

    public Mutter createAndReturn(Mutter mutter) {
        return gateway.createMutter(mutter);
    }

    public boolean create(Mutter mutter) {
        return createAndReturn(mutter) != null;
    }

    public boolean update(Mutter mutter) {
        return gateway.updateMutter(mutter);
    }

    public boolean delete(int mutterId, int userId) {
        return gateway.deleteMutter(mutterId, userId);
    }

    public List<Mutter> search(String keyword) {
        return gateway.searchMutters(keyword);
    }

    private MutterPage toMutterPage(List<Mutter> fetched, int limit) {
        boolean hasNext = fetched.size() > limit;
        List<Mutter> mutters = new ArrayList<>(
                fetched.subList(0, Math.min(limit, fetched.size())));
        Integer nextCursor = hasNext && !mutters.isEmpty()
                ? mutters.get(mutters.size() - 1).getId() : null;
        return new MutterPage(mutters, nextCursor, hasNext);
    }
}
