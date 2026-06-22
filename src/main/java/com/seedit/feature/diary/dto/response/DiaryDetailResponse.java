package com.seedit.feature.diary.dto.response;

import com.seedit.feature.diary.domain.Diary;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DiaryDetailResponse(
        Long did,
        LocalDate diaryDate,
        String content,
        String aiFeedback,
        LocalDateTime createdAt
) {
    public static DiaryDetailResponse from(Diary diary) {
        return new DiaryDetailResponse(
                diary.getDid(),
                diary.getDiaryDate(),
                diary.getContent(),
                diary.getAiFeedback(),
                diary.getCreatedAt()
        );
    }
}
