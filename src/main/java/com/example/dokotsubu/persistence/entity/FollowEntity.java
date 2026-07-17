package com.example.dokotsubu.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("follows")
public record FollowEntity(
        @Id Integer id,
        @Column("follower_id") int followerId,
        @Column("followee_id") int followeeId) {
}
