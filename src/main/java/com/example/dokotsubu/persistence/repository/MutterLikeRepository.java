package com.example.dokotsubu.persistence.repository;

import com.example.dokotsubu.persistence.entity.MutterLikeEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MutterLikeRepository extends CrudRepository<MutterLikeEntity, Integer> {
    boolean existsByMutterIdAndUserId(int mutterId, int userId);

    long countByMutterId(int mutterId);

    @Modifying
    @Query("DELETE FROM mutter_likes WHERE mutter_id = :mutterId AND user_id = :userId")
    boolean remove(@Param("mutterId") int mutterId, @Param("userId") int userId);
}
