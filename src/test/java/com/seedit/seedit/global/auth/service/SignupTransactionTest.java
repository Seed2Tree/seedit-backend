package com.seedit.seedit.global.auth.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.level.repository.UserLevelRepository;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.global.auth.dto.request.SignUpRequest;
import com.seedit.global.auth.dto.response.SignupResponse;
import com.seedit.global.auth.service.AuthService;
import com.seedit.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("회원가입 트랜잭션 테스트")
class SignupTransactionTest {

    @Autowired
    AuthService authService;
    @Autowired UserLevelRepository userLevelRepository;
    @Autowired BalanceHistoryRepository balanceHistoryRepository;

    private SignUpRequest request(String email) {
        return new SignUpRequest("tester", "abcd1234!", "테스터",
                LocalDate.of(2000, 1, 1), email);
    }

    @Test
    @DisplayName("회원가입 성공 시 user_level과 balance_history(INIT)가 함께 생성된다")
    void signup_createsLevelAndBalanceTogether() {
        SignupResponse res = authService.signup(request("tx-" + System.nanoTime() + "@seedit.com"));

        // 레벨 생성 확인
        assertThat(userLevelRepository.selectByUserId(res.userId())).isPresent();
        assertThat(userLevelRepository.selectByUserId(res.userId()).get().getLevel()).isEqualTo(1);

        // 잔액 이력 확인 (초기 1건, INIT, 500만)
        List<BalanceHistory> histories = balanceHistoryRepository.findByUserId(res.userId());
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getReasonType()).isEqualTo(TradeType.INIT);
        assertThat(histories.get(0).getCurrentBalance()).isEqualTo(5_000_000L);
    }

    @Test
    @DisplayName("중복 이메일이면 BusinessException이 발생한다")
    void signup_duplicateEmail_throws() {
        String email = "dup-" + System.nanoTime() + "@seedit.com";
        authService.signup(request(email));

        assertThatThrownBy(() -> authService.signup(request(email)))
                .isInstanceOf(BusinessException.class);
    }
}