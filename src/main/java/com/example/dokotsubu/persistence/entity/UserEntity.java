package com.example.dokotsubu.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record UserEntity(
        @Id Integer id,
        String name,
        String pass,
        String bio) {
}
