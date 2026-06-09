package com.seedit.feature.user.dto.response;

import com.seedit.feature.user.domain.UserAccount;
import lombok.Data;

@Data
public class UserProfileResponse {
    /**
     * 내 정보 상세 조회 응답 (비밀번호 제외, 잔액 포함)
     */
    private int userId;
    private String username;
    private String name;
    private String birth;
    private String email;
    private double balance;
    private double totalInvested;
    private String createdAt;

    public UserProfileResponse(UserAccount account){
        this.userId = account.getUserId();
        this.username = account.getUsername();
        this.name = account.getName();
        this.birth = account.getBirth();
        this.email = account.getEmail();
        this.balance = account.getBalance();
        this.totalInvested = account.getTotalInvested();
        this.createdAt = account.getCreatedAt();
    }
}
