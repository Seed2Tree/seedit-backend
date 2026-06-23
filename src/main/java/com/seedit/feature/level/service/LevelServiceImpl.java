package com.seedit.feature.level.service;

import com.seedit.feature.level.domain.LevelDefinition;
import com.seedit.feature.level.domain.PointReason;
import com.seedit.feature.level.domain.UserLevel;
import com.seedit.feature.level.repository.LevelDefinitionRepository;
import com.seedit.feature.level.repository.UserLevelRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LevelServiceImpl implements LevelService {

    private final UserLevelRepository userLevelRepository;
    private final LevelDefinitionRepository levelDefinitionRepository;

    @Override
    @Transactional
    public void addPoint(Long userId, PointReason reason) {
        UserLevel ul = userLevelRepository.selectByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레벨 정보가 없습니다."));
        int newPoint = ul.getPoint() + reason.getPoint();
        int newLevel = resolveLevel(newPoint);            // 누적 포인트로 레벨 재판정
        userLevelRepository.updatePointAndLevel(userId, newPoint, newLevel);
    }

    /** required_point <= 누적포인트 인 레벨 중 가장 높은 레벨 */
    private int resolveLevel(int point) {
        return levelDefinitionRepository.findAll().stream()
                .filter(d -> d.getRequiredPoint() <= point)
                .mapToInt(LevelDefinition::getLevel)
                .max().orElse(1);
    }

    @Override
    public int getNextLevelPoint(int currentLevel) {
        LevelDefinition next = levelDefinitionRepository.findOneByLevel(currentLevel + 1);
        if (next != null) return next.getRequiredPoint();          // 다음 레벨 기준
        LevelDefinition cur = levelDefinitionRepository.findOneByLevel(currentLevel);
        return cur != null ? cur.getRequiredPoint() : 0;           // 최고 레벨이면 현재 기준
    }
}