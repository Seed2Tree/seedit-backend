package com.seedit.feature.news.controller;

import com.seedit.feature.news.domain.NewsContent;
import com.seedit.feature.news.service.NewsService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name="뉴스 RSS API", description = "한경/매일/서울 경제 RSS API 호출")
public class NewsController {
    private final NewsService newsService;

    @PostMapping("/collect")
    public ApiResponse<Integer> collect(){
        int res = newsService.collectAndSave();
        return ApiResponse.ok(res);
    }

    @GetMapping
    public ApiResponse<List<NewsContent>> byStock(@RequestParam String companyName){
        List<NewsContent> newsContents = newsService.findByCompanyName(companyName);
        return ApiResponse.ok(newsContents);
    }

    @GetMapping("/date")
    public ApiResponse<List<NewsContent>> byDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        List<NewsContent> newsContents = newsService.findByDate(date);
        return ApiResponse.ok(newsContents);
    }
}
