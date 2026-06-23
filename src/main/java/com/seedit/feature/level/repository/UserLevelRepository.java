package com.seedit.feature.level.repository;

import com.seedit.feature.level.domain.UserLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserLevelRepository {
    int insertDefaultLevel(@Param("userId") Long userId);

    Optional<UserLevel> selectByUserId(@Param("userId") Long userId);

    int updatePointAndLevel(@Param("userId") Long userId,
                            @Param("point") int point,
                            @Param("level") int level);
}
