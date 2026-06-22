package com.seedit.feature.diary.controller;

import com.seedit.feature.diary.dto.request.DiaryCreateRequest;
import com.seedit.feature.diary.dto.request.DiaryUpdateRequest;
import com.seedit.feature.diary.dto.response.DiaryDetailResponse;
import com.seedit.feature.diary.dto.response.DiaryListItem;
import com.seedit.feature.diary.service.DiaryService;
import com.seedit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 전체 일지 목록
    @GetMapping
    public ApiResponse<List<DiaryListItem>> getList(Authentication authentication) {
        return ApiResponse.ok(diaryService.getList(authentication.getName()));
    }

    // 캘린더용 월별 일지 날짜 목록
    @GetMapping("/calendar")
    public ApiResponse<List<LocalDate>> getCalendar(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ApiResponse.ok(diaryService.getCalendar(authentication.getName(), year, month));
    }

    // 날짜별 일지 상세 조회 (없으면 null)
    @GetMapping("/{date}")
    public ApiResponse<DiaryDetailResponse> getByDate(
            Authentication authentication,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(diaryService.getByDate(authentication.getName(), date));
    }

    // 일지 작성
    @PostMapping
    public ApiResponse<DiaryDetailResponse> create(
            Authentication authentication,
            @RequestBody DiaryCreateRequest request
    ) {
        return ApiResponse.ok(diaryService.create(authentication.getName(), request));
    }

    // 일지 수정
    @PatchMapping("/{did}")
    public ApiResponse<DiaryDetailResponse> update(
            Authentication authentication,
            @PathVariable Long did,
            @RequestBody DiaryUpdateRequest request
    ) {
        return ApiResponse.ok(diaryService.update(authentication.getName(), did, request));
    }

    // 일지 삭제
    @DeleteMapping("/{did}")
    public ApiResponse<Void> delete(
            Authentication authentication,
            @PathVariable Long did
    ) {
        diaryService.delete(authentication.getName(), did);
        return ApiResponse.ok();
    }
}
