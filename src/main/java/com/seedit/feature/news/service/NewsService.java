package com.seedit.feature.news.service;

import com.seedit.feature.news.domain.NewsContent;

import java.time.LocalDate;
import java.util.List;

public interface NewsService {
    int collectAndSave();
    List<NewsContent> findByCompanyName(String companyName);
    List<NewsContent> findByDate(LocalDate date);
}
