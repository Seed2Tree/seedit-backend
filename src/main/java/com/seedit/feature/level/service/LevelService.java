package com.seedit.feature.level.service;

import com.seedit.feature.level.domain.PointReason;
import org.apache.ibatis.annotations.Param;

public interface LevelService {
    void addPoint(Long userId, PointReason reason);
    int getNextLevelPoint(int currentLevel); // 다음 레벨 required_point
}