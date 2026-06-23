package com.seedit.feature.study.service;

import com.seedit.feature.study.dto.response.StudyDetailResponse;
import com.seedit.feature.study.dto.response.StudyListResponse;
import com.seedit.feature.study.repository.StudyRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;
    private final UserAccountRepository userAccountRepository;

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

    @Override
    public void addBookmark(String email, Long isid) {
        Long userId = getUserId(email);
        if (studyRepository.findById(isid).isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "존재하지 않는 콘텐츠입니다.");
        }
        if (studyRepository.countBookmark(userId, isid) > 0) {
            throw new BusinessException(ErrorCode.STUDY_BOOKMARK_DUPLICATED, "이미 즐겨찾기한 콘텐츠입니다.");
        }
        studyRepository.insertBookmark(userId, isid);
    }

    @Override
    public void removeBookmark(String email, Long isid) {
        Long userId = getUserId(email);
        studyRepository.deleteBookmark(userId, isid);
    }

    @Override
    public List<StudyListResponse> getMyBookmarks(String email) {
        Long userId = getUserId(email);
        return studyRepository.findBookmarkedByUserId(userId).stream()
                .map(StudyListResponse::from)
                .toList();
    }

    @Override
    public List<Long> getMyBookmarkIds(String email) {
        Long userId = getUserId(email);
        return studyRepository.findBookmarkIdsByUserId(userId);
    }

    private Long getUserId(String email) {
        return userAccountRepository.findUserByEmail(email)
                .map(UserAccount::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
