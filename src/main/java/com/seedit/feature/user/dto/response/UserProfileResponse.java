package com.seedit.feature.user.dto.response;

import com.seedit.feature.user.domain.UserAccount;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 내 정보 상세 조회 응답 (비밀번호 제외, 잔액 포함)
 */
public record UserProfileResponse (
        Long userId,
        String username,
        String email,
        String name,
        Long balance,
        Long totalInvested,
        LocalDate birth,
        LocalDateTime createdAt,
        LevelInfo level,
        ActivityInfo activity

){

    public record LevelInfo(int level, String levelName, int point, int nextLevelPoint) {}

    public record ActivityInfo (
            int transactionCount,
            int diaryCount,
            int watchlistCount,
            int studyBookmarkCount
    ){}

    public static UserProfileResponse from(UserAccount account, LevelInfo level, ActivityInfo activity){
        return new UserProfileResponse(
                account.getUserId(),
                account.getUsername(),
                account.getEmail(),
                account.getName(),
                account.getBalance(),
                account.getTotalInvested(),
                account.getBirth(),
                account.getCreatedAt(),
                level,
                activity
        );
    }


}
