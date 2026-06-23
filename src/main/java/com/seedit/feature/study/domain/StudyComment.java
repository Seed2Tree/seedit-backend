package com.seedit.feature.study.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudyComment {
    private Long scid;
    private Long isid;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
