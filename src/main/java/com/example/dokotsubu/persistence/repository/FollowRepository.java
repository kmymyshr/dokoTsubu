package com.example.dokotsubu.persistence.repository;

import com.example.dokotsubu.persistence.entity.FollowEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends CrudRepository<FollowEntity, Integer> {
    boolean existsByFollowerIdAndFolloweeId(int followerId, int followeeId);

    long countByFolloweeId(int followeeId);

    long countByFollowerId(int followerId);

    @Modifying
    @Query("DELETE FROM follows WHERE follower_id = :followerId AND followee_id = :followeeId")
    boolean remove(@Param("followerId") int followerId, @Param("followeeId") int followeeId);
}
