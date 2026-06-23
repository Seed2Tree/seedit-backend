package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.response.StudyDetailResponse;
import com.seedit.feature.study.dto.response.StudyListResponse;

import java.util.List;

public interface StudyService {

    List<StudyListResponse> getList(String category);

    StudyDetailResponse getDetail(Long isid);

    void addBookmark(String email, Long isid);

    void removeBookmark(String email, Long isid);

    List<StudyListResponse> getMyBookmarks(String email);

    List<Long> getMyBookmarkIds(String email);
}
