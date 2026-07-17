package com.example.dokotsubu.persistence.repository;

import com.example.dokotsubu.persistence.entity.MutterEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MutterRepository extends CrudRepository<MutterEntity, Integer> {

    @Modifying
    @Query("""
            UPDATE mutters
            SET text = :text, version = version + 1
            WHERE id = :id AND user_id = :userId AND version = :version
            """)
    boolean updateOwned(
            @Param("id") int id,
            @Param("userId") int userId,
            @Param("text") String text,
            @Param("version") int version);

    @Modifying
    @Query("DELETE FROM mutters WHERE id = :id AND user_id = :userId")
    boolean deleteOwned(@Param("id") int id, @Param("userId") int userId);
}
