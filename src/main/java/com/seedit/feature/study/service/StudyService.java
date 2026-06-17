package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.response.StudyDetailResponse;
import com.seedit.feature.study.dto.response.StudyListResponse;

import java.util.List;

public interface StudyService {

    List<StudyListResponse> getList(String category);

    StudyDetailResponse getDetail(Long isid);
}
