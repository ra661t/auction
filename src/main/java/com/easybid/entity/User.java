package com.easybid.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", unique = true, nullable = false)
    private String email;

    @Column(name = "user_pw", nullable = false)
    private String password;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(name = "user_phone")
    private String phone;

    @Column(name = "user_image")
    private String imageUrl; // 이미지 URL
}
