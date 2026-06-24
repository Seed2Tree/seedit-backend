package com.seedit.feature.news.service;

import com.seedit.feature.news.domain.NewsContent;
import com.seedit.feature.news.external.NewsCollector;
import com.seedit.feature.news.external.RssArticle;
import com.seedit.feature.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService{

    private final NewsCollector newsCollector;
    private final NewsRepository newsRepository;

    @Transactional
    @Override
    public int collectAndSave() {
        try {
            List<RssArticle> articles = newsCollector.collectAll();
        int saved = 0;
        for (RssArticle a : articles){
            if(a.getLink() == null || a.getTitle() == null) continue;
            NewsContent news = new NewsContent();
            news.setNewsTitle(a.getTitle());
            news.setNewsUrl(a.getLink());
            news.setPress(a.getPress());
            news.setPublishedAt(a.getPubDate());
            saved += newsRepository.insertIgnore(news);
        }
            return saved;
        }
        catch (Exception e){
            // 에러
            return 0;
        }
    }

    @Override
    public List<NewsContent> findByCompanyName(String companyName) {
        return newsRepository.findByCompanyName(companyName);
    }

    @Override
    public List<NewsContent> findByDate(LocalDate date) {
        return newsRepository.findByDate(date);
    }
}
