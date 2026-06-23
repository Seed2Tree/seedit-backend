package com.seedit.feature.news.external;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @AllArgsConstructor
public class RssArticle {
    private final String title;
    private final String link;
    private final LocalDateTime pubDate;
    private final String press;
}
