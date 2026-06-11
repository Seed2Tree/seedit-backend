package com.seedit.feature.level.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevel {
    private Long userId;
    private int level;
    private int point;
    private LocalDateTime updatedAt;
}
