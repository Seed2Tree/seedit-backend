package com.seedit.feature.news.external;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix ="app.rss")
public record RssProperties(List<Feed> feeds) {
    public record Feed(String press, String url){}
}
