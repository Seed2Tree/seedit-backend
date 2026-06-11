package com.seedit.feature.level.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelDefinition {
    private int level;
    private String levelName;
    private int requiredPoint;
    private String benefits;
}
