package com.seedit.feature.watchlist.domain;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Watchlist {
    private Long wid;
    private Long userId;
    private Long sid;
    private LocalDateTime createdAt;
}
