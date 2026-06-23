package com.seedit.feature.level.domain;

import lombok.Getter;

@Getter
public enum PointReason {
    TRADE(10),               // 매수/매도 체결
    DIARY(20),               // 투자 일지 작성
    HYPOTHESIS_VERIFIED(30), // 매수 가설 회고 완료(추후)
    WATCHLIST_ADD(5);        // 관심종목 추가

    private final int point;
    PointReason(int point) { this.point = point; }
}