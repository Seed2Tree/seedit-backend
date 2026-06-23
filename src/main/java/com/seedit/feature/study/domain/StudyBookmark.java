package com.seedit.feature.study.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudyBookmark {
    private Long sbid;
    private Long userId;
    private Long isid;
    private LocalDateTime createdAt;
}
