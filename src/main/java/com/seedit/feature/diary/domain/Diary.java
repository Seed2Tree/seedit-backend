package com.seedit.feature.diary.domain;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Diary {
    private Long did;
    private Long userId;
    private LocalDate diaryDate;
    private String content;
    private String aiFeedback;
    private LocalDateTime createdAt;
}
