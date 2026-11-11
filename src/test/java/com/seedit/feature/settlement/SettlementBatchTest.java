package com.seedit.feature.settlement;

import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.settlement.service.*;
import com.seedit.feature.user.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.sql.SQLException;
import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SettlementBatchTest {
    @Test
    void infrastructureFailureStopsBeforeConsumingOtherItems() {
        var repository = mock(SettlementRepository.class);
        var processor = mock(SettlementProcessor.class);
        Clock clock = Clock.fixed(Instant.parse("2026-11-10T15:10:00Z"), ZoneId.of("Asia/Seoul"));
        var service = new SettlementServiceImpl(repository, mock(UserAccountRepository.class), processor, clock);
        when(repository.findDuePage(any(), any(), eq(0L), eq(200), eq(false)))
                .thenReturn(List.of(Settlement.builder().settlementId(1L).userId(1L).build(),
                        Settlement.builder().settlementId(2L).userId(2L).build()));
        var attempt = new SettlementProcessor.Attempt(1L, 1L, "token");
        when(processor.claim(eq(1L), eq(1L), any())).thenReturn(attempt);
        when(processor.settleOne(attempt)).thenThrow(new CannotCreateTransactionException("DB unavailable"));
        assertThatThrownBy(() -> service.settleDue(LocalDate.now(clock)))
                .isInstanceOf(CannotCreateTransactionException.class);
        verify(processor, never()).claim(eq(2L), anyLong(), any());
        verify(processor, never()).recordFailure(any(), anyBoolean(), anyString());
    }

    @Test
    void retryClassificationIsLimitedToTransientLockErrors() {
        assertThat(SettlementRetryPolicy.isRetryable(new CannotAcquireLockException("locked"))).isTrue();
        assertThat(SettlementRetryPolicy.isRetryable(new RuntimeException(new SQLException("deadlock", "40001", 1213)))).isTrue();
        assertThat(SettlementRetryPolicy.isRetryable(new DataIntegrityViolationException("invalid"))).isFalse();
        assertThat(SettlementRetryPolicy.isInfrastructureFailure(new RuntimeException(new SQLException("lost", "08006")))).isTrue();
        assertThat(SettlementRetryPolicy.isInfrastructureFailure(new CannotAcquireLockException("locked"))).isFalse();
    }
}
