package com.seedit.feature.news.external;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NewsCollector {
    private static final Logger log = LoggerFactory.getLogger(NewsCollector.class);
    private final RssParser rssParser;
    private final RssProperties rssProperties;
    private final RestClient restClient = RestClient.create();

    public List<RssArticle> collectAll(){
        List<RssArticle> all = new ArrayList<>();
        for(RssProperties.Feed feed : rssProperties.feeds()) {
            try {
                byte[] xml = restClient.get()
                        .uri(URI.create(feed.url()))
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36")
                        .accept(MediaType.APPLICATION_RSS_XML, MediaType.APPLICATION_XML,MediaType.TEXT_XML)
                        .retrieve()
                        .body(byte[].class);
                List<RssArticle> articles = rssParser.parse(xml, feed.press());
                all.addAll(articles);
                log.info("[뉴스수집] {} - {}건", feed.press(), articles.size());
            } catch (Exception e) {
                log.error("[뉴스수집] 실패 : {}", e);
            }
        }
        return all;
    }
}
