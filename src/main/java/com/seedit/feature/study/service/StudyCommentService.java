package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.request.StudyCommentRequest;
import com.seedit.feature.study.dto.response.StudyCommentResponse;

import java.util.List;

public interface StudyCommentService {
    List<StudyCommentResponse> getComments(Long isid, String email);
    void addComment(Long isid, String email, StudyCommentRequest request);
    void updateComment(Long scid, String email, StudyCommentRequest request);
    void deleteComment(Long scid, String email);
}
