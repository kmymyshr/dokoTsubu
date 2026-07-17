package com.example.dokotsubu.persistence.repository;

import java.util.Optional;

import com.example.dokotsubu.persistence.entity.UserEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    Optional<UserEntity> findByName(String name);

    @Modifying
    @Query("UPDATE users SET bio = :bio WHERE id = :id")
    boolean updateBio(@Param("id") int id, @Param("bio") String bio);
}
