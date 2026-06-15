package com.fb.auth.model;

import com.fb.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * Người dùng trong hệ thống
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(length = 500)
    private String avatar;

    @Column(length = 500)
    private String coverPhoto;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    private LocalDate birthday;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String workplace;

    @Column(length = 100)
    private String education;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean enabled = true;
}
