package com.seedit.feature.study.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InvestmentStudy {
    private Long isid;
    private String title;
    private String youtubeUrl;
    private String description;
    private String category;
    private String thumbnail;
    private LocalDateTime createdAt;
}
