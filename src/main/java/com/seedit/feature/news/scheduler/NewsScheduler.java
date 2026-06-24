package com.seedit.feature.news.scheduler;

import com.seedit.feature.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private static final Logger log = LoggerFactory.getLogger(NewsScheduler.class);
    private final NewsService newsService;

    @Scheduled(cron = "0 0 * * * *", zone="Asia/Seoul")
    public void collect(){
        try{
            int saved = newsService.collectAndSave();
            log.info("[뉴스배치] 수집 완료 - 신규 {}건", saved);
        } catch(Exception e){
            log.error("[뉴스배치] 수집 실패", e);
        }
    }
}
