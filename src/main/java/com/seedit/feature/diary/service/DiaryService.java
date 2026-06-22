package com.seedit.feature.diary.service;

import com.seedit.feature.diary.dto.request.DiaryCreateRequest;
import com.seedit.feature.diary.dto.request.DiaryUpdateRequest;
import com.seedit.feature.diary.dto.response.DiaryDetailResponse;
import com.seedit.feature.diary.dto.response.DiaryListItem;
import com.seedit.feature.diary.repository.DiaryRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserAccountRepository userAccountRepository;

    public List<DiaryListItem> getList(String email) {
        Long userId = getUserId(email);
        return diaryRepository.findAllByUserId(userId).stream()
                .map(DiaryListItem::from)
                .toList();
    }

    public List<LocalDate> getCalendar(String email, int year, int month) {
        Long userId = getUserId(email);
        return diaryRepository.findDatesByUserIdAndMonth(userId, year, month);
    }

    public DiaryDetailResponse getByDate(String email, LocalDate date) {
        Long userId = getUserId(email);
        return diaryRepository.findByUserIdAndDate(userId, date)
                .map(DiaryDetailResponse::from)
                .orElse(null);
    }

    public DiaryDetailResponse create(String email, DiaryCreateRequest request) {
        Long userId = getUserId(email);
        if (diaryRepository.countByUserIdAndDate(userId, request.diaryDate()) > 0) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_WRITTEN_TODAY, "해당 날짜에 이미 일지를 작성했습니다.");
        }
        diaryRepository.insert(userId, request.diaryDate(), request.content());
        return diaryRepository.findByUserIdAndDate(userId, request.diaryDate())
                .map(DiaryDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "일지 저장에 실패했습니다."));
    }

    public DiaryDetailResponse update(String email, Long did, DiaryUpdateRequest request) {
        Long userId = getUserId(email);
        diaryRepository.findByDidAndUserId(did, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "일지를 찾을 수 없습니다."));
        diaryRepository.update(did, userId, request.content());
        return diaryRepository.findByDidAndUserId(did, userId)
                .map(DiaryDetailResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_INTERNAL, "일지 수정에 실패했습니다."));
    }

    public void delete(String email, Long did) {
        Long userId = getUserId(email);
        diaryRepository.findByDidAndUserId(did, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "일지를 찾을 수 없습니다."));
        diaryRepository.delete(did, userId);
    }

    private Long getUserId(String email) {
        return userAccountRepository.findUserByEmail(email)
                .map(UserAccount::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
