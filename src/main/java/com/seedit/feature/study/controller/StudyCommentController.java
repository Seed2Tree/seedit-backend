package com.seedit.feature.study.controller;

import com.seedit.feature.study.dto.request.StudyCommentRequest;
import com.seedit.feature.study.dto.response.StudyCommentResponse;
import com.seedit.feature.study.service.StudyCommentService;
import com.seedit.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study/{isid}/comments")
@RequiredArgsConstructor
@Tag(name = "투자공부 댓글 API")
public class StudyCommentController {

    private final StudyCommentService commentService;

    @GetMapping
    public ApiResponse<List<StudyCommentResponse>> getComments(
            @PathVariable Long isid,
            Authentication auth
    ) {
        return ApiResponse.ok(commentService.getComments(isid, auth.getName()));
    }

    @PostMapping
    public ApiResponse<Void> addComment(
            @PathVariable Long isid,
            @RequestBody StudyCommentRequest request,
            Authentication auth
    ) {
        commentService.addComment(isid, auth.getName(), request);
        return ApiResponse.ok();
    }

    @PutMapping("/{scid}")
    public ApiResponse<Void> updateComment(
            @PathVariable Long isid,
            @PathVariable Long scid,
            @RequestBody StudyCommentRequest request,
            Authentication auth
    ) {
        commentService.updateComment(scid, auth.getName(), request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{scid}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long isid,
            @PathVariable Long scid,
            Authentication auth
    ) {
        commentService.deleteComment(scid, auth.getName());
        return ApiResponse.ok();
    }
}
