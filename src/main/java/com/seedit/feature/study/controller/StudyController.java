package com.seedit.feature.study.controller;

import com.seedit.feature.study.dto.response.StudyDetailResponse;
import com.seedit.feature.study.dto.response.StudyListResponse;
import com.seedit.feature.study.service.StudyService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
@Tag(name = "투자공부 API", description = "선별된 금융 교육 영상 목록 및 상세 조회")
public class StudyController {

    private final StudyService studyService;

    @GetMapping
    public ApiResponse<List<StudyListResponse>> getList(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.ok(studyService.getList(category));
    }

    @GetMapping("/{isid}")
    public ApiResponse<StudyDetailResponse> getDetail(@PathVariable Long isid) {
        return ApiResponse.ok(studyService.getDetail(isid));
    }
}
