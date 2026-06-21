package com.seedit.feature.diary.dto.response;

import com.seedit.feature.diary.domain.Diary;

import java.time.LocalDate;

public record DiaryListItem(
        Long did,
        LocalDate diaryDate,
        String contentPreview
) {
    public static DiaryListItem from(Diary diary) {
        String preview = diary.getContent() == null ? null
                : diary.getContent().length() > 50 ? diary.getContent().substring(0, 50) + "..."
                : diary.getContent();
        return new DiaryListItem(diary.getDid(), diary.getDiaryDate(), preview);
    }
}
