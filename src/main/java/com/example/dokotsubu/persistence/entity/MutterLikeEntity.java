package com.example.dokotsubu.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("mutter_likes")
public record MutterLikeEntity(
        @Id Integer id,
        @Column("mutter_id") int mutterId,
        @Column("user_id") int userId) {
}
