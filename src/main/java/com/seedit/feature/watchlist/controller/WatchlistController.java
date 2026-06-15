package com.seedit.feature.watchlist.controller;

import com.seedit.feature.watchlist.dto.request.WatchlistAddRequest;
import com.seedit.feature.watchlist.dto.response.WatchlistResponse;
import com.seedit.feature.watchlist.service.WatchlistService;
import com.seedit.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping
    public ApiResponse<List<WatchlistResponse>> getList(Authentication authentication) {
        return ApiResponse.ok(watchlistService.getList(authentication.getName()));
    }

    @PostMapping
    public ApiResponse<Void> add(Authentication authentication, @RequestBody WatchlistAddRequest request) {
        watchlistService.add(authentication.getName(), request.sid());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{sid}")
    public ApiResponse<Void> remove(Authentication authentication, @PathVariable Long sid) {
        watchlistService.remove(authentication.getName(), sid);
        return ApiResponse.ok();
    }
}
