package com.seedit.feature.level.repository;

import com.seedit.feature.level.domain.LevelDefinition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LevelDefinitionRepository {
    String findLevelNameByLevel(int level);

    LevelDefinition findOneByLevel(int level);

    List<LevelDefinition> findAll();
}
