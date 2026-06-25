package com.seedit.feature.reason.service;

import com.seedit.feature.level.domain.PointReason;
import com.seedit.feature.level.service.LevelService;
import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.reason.repository.ReasonRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReasonServiceImpl implements ReasonService{

    private final ReasonRepository reasonRepository;
    private final UserAccountRepository userAccountRepository;
    private final LevelService levelService;

    @Override
    @Transactional
    public void verify(String email, Long rid, Long sellTid){
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Reason reason = reasonRepository.findByUserIdAndId(user.getUserId(), rid)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "가설을 찾을 수 없습니다."));

        // 이미 검증된 가설이면 멱등 처리 (중복 30P 방지)
        if (Boolean.TRUE.equals(reason.getIsVerified())) return;

        reasonRepository.updateVerified(user.getUserId(), rid, sellTid);
        levelService.addPoint(user.getUserId(), PointReason.HYPOTHESIS_VERIFIED); // 기획에 이미 있는 30P
    }
}
