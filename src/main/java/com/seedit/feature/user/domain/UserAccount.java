package com.seedit.feature.user.domain;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private Long userId;
    private String username;
    private String passwordHash; // BCrypt 해시값
    private String name;
    private LocalDate birth;
    private String email;
    private Long balance;
    private Long totalInvested;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Role role; // "ROLE_USER", "ROLE_ADMIN" (Default "ROLE_USER")
}