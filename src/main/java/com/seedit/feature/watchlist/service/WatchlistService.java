package com.seedit.feature.watchlist.service;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.feature.watchlist.dto.response.WatchlistResponse;
import com.seedit.feature.watchlist.repository.WatchlistRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserAccountRepository userAccountRepository;

    public List<WatchlistResponse> getList(String email) {
        Long userId = getUserId(email);
        return watchlistRepository.findByUserId(userId).stream()
                .map(WatchlistResponse::new)
                .toList();
    }

    public void add(String email, Long sid) {
        Long userId = getUserId(email);
        if (watchlistRepository.countStockBySid(sid) == 0) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "존재하지 않는 종목입니다.");
        }
        if (watchlistRepository.countByUserIdAndSid(userId, sid) > 0) {
            throw new BusinessException(ErrorCode.WATCHLIST_DUPLICATED, "이미 등록된 관심 종목입니다.");
        }
        watchlistRepository.insert(userId, sid);
    }

    public void remove(String email, Long sid) {
        Long userId = getUserId(email);
        watchlistRepository.deleteByUserIdAndSid(userId, sid);
    }

    private Long getUserId(String email) {
        return userAccountRepository.findUserByEmail(email)
                .map(UserAccount::getUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
