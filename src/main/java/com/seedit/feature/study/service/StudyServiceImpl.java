package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.response.StudyDetailResponse;
import com.seedit.feature.study.dto.response.StudyListResponse;
import com.seedit.feature.study.repository.StudyRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;

    @Override
    public List<StudyListResponse> getList(String category) {
        var items = (category == null || category.isBlank())
                ? studyRepository.findAll()
                : studyRepository.findByCategory(category);
        return items.stream().map(StudyListResponse::from).toList();
    }

    @Override
    public StudyDetailResponse getDetail(Long isid) {
        return studyRepository.findById(isid)
                .map(StudyDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "콘텐츠를 찾을 수 없습니다."));
    }
}
