package com.seedit.feature.news.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsContent {
    Long nid;
    String newsTitle;
    String newsUrl;
    String press;
    LocalDateTime publishedAt;
    LocalDateTime createdAt;
}
