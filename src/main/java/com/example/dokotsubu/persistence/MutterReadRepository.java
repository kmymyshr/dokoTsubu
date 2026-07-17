package com.example.dokotsubu.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import model.Mutter;
import model.MutterFeedItem;
import model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MutterReadRepository {
    private static final String MUTTER_COLUMNS =
            "m.id, m.user_id, u.name, m.text, m.version, m.created_at ";

    private final NamedParameterJdbcTemplate jdbc;

    public MutterReadRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Mutter> findLatest(int limit) {
        String sql = "SELECT " + MUTTER_COLUMNS
                + "FROM mutters m JOIN users u ON m.user_id = u.id "
                + "ORDER BY m.id DESC LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource("limit", limit), this::toMutter);
    }

    public List<Mutter> findByCursor(int cursor, int limit) {
        String sql = "SELECT " + MUTTER_COLUMNS
                + "FROM mutters m JOIN users u ON m.user_id = u.id "
                + "WHERE m.id < :cursor ORDER BY m.id DESC LIMIT :limit";
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("cursor", cursor)
                .addValue("limit", limit), this::toMutter);
    }

    public List<Mutter> findPage(String keyword, Integer cursor, int limit) {
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(MUTTER_COLUMNS)
                .append("FROM mutters m JOIN users u ON m.user_id = u.id WHERE 1 = 1 ");
        MapSqlParameterSource parameters = new MapSqlParameterSource("limit", limit);
        if (keyword != null) {
            sql.append("AND m.text LIKE :keyword ");
            parameters.addValue("keyword", "%" + keyword + "%");
        }
        if (cursor != null) {
            sql.append("AND m.id < :cursor ");
            parameters.addValue("cursor", cursor);
        }
        sql.append("ORDER BY m.id DESC LIMIT :limit");
        return jdbc.query(sql.toString(), parameters, this::toMutter);
    }

    public List<MutterFeedItem> findFeedPage(
            String keyword, Integer cursor, int limit, int viewerId) {
        StringBuilder sql = new StringBuilder(
                "WITH page AS (SELECT " + MUTTER_COLUMNS
                        + "FROM mutters m JOIN users u ON m.user_id = u.id WHERE 1 = 1 ");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("viewerId", viewerId);
        if (keyword != null) {
            sql.append("AND m.text LIKE :keyword ");
            parameters.addValue("keyword", "%" + keyword + "%");
        }
        if (cursor != null) {
            sql.append("AND m.id < :cursor ");
            parameters.addValue("cursor", cursor);
        }
        sql.append("ORDER BY m.id DESC LIMIT :limit) ")
                .append("SELECT p.id, p.user_id, p.name, p.text, p.version, p.created_at, ")
                .append("(SELECT COUNT(*) FROM mutter_likes ml WHERE ml.mutter_id = p.id) AS like_count, ")
                .append("EXISTS (SELECT 1 FROM mutter_likes ml ")
                .append("WHERE ml.mutter_id = p.id AND ml.user_id = :viewerId) AS liked_by_me, ")
                .append("EXISTS (SELECT 1 FROM follows f ")
                .append("WHERE f.follower_id = :viewerId AND f.followee_id = p.user_id) AS followed_by_me ")
                .append("FROM page p ORDER BY p.id DESC");
        return jdbc.query(sql.toString(), parameters, (rs, rowNum) -> new MutterFeedItem(
                toMutter(rs, rowNum),
                rs.getInt("like_count"),
                rs.getBoolean("liked_by_me"),
                rs.getBoolean("followed_by_me")));
    }

    public Mutter findById(int mutterId) {
        String sql = "SELECT " + MUTTER_COLUMNS
                + "FROM mutters m JOIN users u ON m.user_id = u.id WHERE m.id = :id";
        return jdbc.query(sql, new MapSqlParameterSource("id", mutterId), this::toMutter)
                .stream().findFirst().orElse(null);
    }

    public List<Mutter> search(String keyword) {
        String sql = "SELECT " + MUTTER_COLUMNS
                + "FROM mutters m JOIN users u ON m.user_id = u.id "
                + "WHERE m.text LIKE :keyword ORDER BY m.id DESC";
        return jdbc.query(sql, new MapSqlParameterSource("keyword", "%" + keyword + "%"), this::toMutter);
    }

    public List<User> findFollowingUsers(int userId) {
        String sql = """
                SELECT u.id, u.name, u.pass, u.bio
                FROM follows f
                JOIN users u ON f.followee_id = u.id
                WHERE f.follower_id = :userId
                ORDER BY u.name ASC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("userId", userId), userRowMapper());
    }

    public List<User> findFollowerUsers(int userId) {
        String sql = """
                SELECT u.id, u.name, u.pass, u.bio
                FROM follows f
                JOIN users u ON f.follower_id = u.id
                WHERE f.followee_id = :userId
                ORDER BY u.name ASC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("userId", userId), userRowMapper());
    }

    private Mutter toMutter(ResultSet rs, int rowNum) throws SQLException {
        return new Mutter(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("text"),
                rs.getInt("version"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("pass"),
                rs.getString("bio"));
    }
}
