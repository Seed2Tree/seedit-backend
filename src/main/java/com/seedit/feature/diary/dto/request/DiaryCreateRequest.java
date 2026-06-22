package com.seedit.feature.diary.dto.request;

import java.time.LocalDate;

public record DiaryCreateRequest(
        LocalDate diaryDate,
        String content
) {}
