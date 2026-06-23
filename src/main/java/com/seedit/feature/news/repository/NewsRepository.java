package com.seedit.feature.news.repository;

import com.seedit.feature.news.domain.NewsContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface NewsRepository {
    int insertIgnore(NewsContent news);
    List<NewsContent> findByCompanyName(@Param("companyName") String companyName);
    List<NewsContent> findByDate(@Param("date") LocalDate date);
}
