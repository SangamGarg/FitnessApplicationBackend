package com.fitnessapp.dev.devUserAndAuthDevService;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_testing_dev")
public class DevUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String devName;

    @Column(unique = true, nullable = false)
    private String devEmail;

    @Column(unique = true, nullable = false)
    private String key;

    @Column(name = "devCreatedAt")
    private String createdAt;


}