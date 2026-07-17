package com.example.dokotsubu.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("mutters")
public record MutterEntity(
        @Id Integer id,
        @Column("user_id") int userId,
        String text,
        @Version Integer version) {
}
