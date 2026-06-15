package com.seedit.feature.watchlist.dto.response;

import com.seedit.feature.watchlist.domain.Watchlist;
import lombok.Getter;

@Getter
public class WatchlistResponse {
    private final Long sid;

    public WatchlistResponse(Watchlist w) {
        this.sid = w.getSid();
    }
}
