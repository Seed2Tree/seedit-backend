package com.seedit.feature.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private int userId;
    private String username;
    private String passwordHash; // BCrypt 해시값
    private String name;
    private String birth;
    private String email;
    private double balance;
    private double totalInvested;
    private String createdAt;
    private String updatedAt;
}

//CREATE TABLE user_account (
//        uid            INT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 고유 ID',
//        username       VARCHAR(50) UNIQUE NOT NULL COMMENT '로그인 아이디',
//password_hash  VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
//name           VARCHAR(50) NOT NULL COMMENT '실명',
//birth          DATE NOT NULL COMMENT '생년월일',
//email          VARCHAR(100) UNIQUE NOT NULL COMMENT '이메일',
//balance        DECIMAL(15,2) NOT NULL DEFAULT 5000000.00 COMMENT '현재 잔액 (모의투자 투자금)',
//total_invested DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '총 투자 원금 누계',
//created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
//updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정일시'
//        ) COMMENT '사용자 계정 및 잔액 정보';

