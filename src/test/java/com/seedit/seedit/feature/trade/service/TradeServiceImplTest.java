package com.seedit.seedit.feature.trade.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.TradeHistoryResponse;
import com.seedit.feature.trade.dto.response.TradeResponse;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.trade.service.TradeServiceImpl;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeService 비즈니스 로직 단위 테스트")
class TradeServiceImplTest {

    @InjectMocks
    private TradeServiceImpl tradeService;

    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    private final String email = "test@seedit.com";
    private UserAccount userAccount;
    private StockDetail stockDetail;

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 세팅
        userAccount = new UserAccount();
        userAccount.setUserId(1L);
        userAccount.setBalance(500000L); // 예수금 50만 원

        stockDetail = new StockDetail();
        stockDetail.setSid(10L);
        stockDetail.setSdid(100L);
        stockDetail.setCurrentPrice(50000L); // 주당 5만 원
    }

    @Nested
    @DisplayName("주문 체결 및 검증 (processOrder)")
    class ProcessOrderTest {

        @Test
        @DisplayName("성공: 매수 주문이 정상적으로 체결되고 잔액이 차감된다.")
        void processOrder_Buy_Success() {
            // given
            TradeRequest request = new TradeRequest(100L, 5, TradeType.BUY); // 5주 매수 = 25만 원
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(stockRepository.findDetailById(request.sdid())).willReturn(stockDetail);

            // when
            TradeResponse response = tradeService.processOrder(email, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.tradeType()).isEqualTo(TradeType.BUY);
            assertThat(response.tradePrice()).isEqualTo(50000L);
            assertThat(response.quantity()).isEqualTo(5);
            assertThat(response.totalAmount()).isEqualTo(250000L);
            assertThat(response.remainingBalance()).isEqualTo(250000L); // 50만 - 25만 = 25만

            // 유저 계좌 상태 변동 및 DB 반영 검증
            assertThat(userAccount.getBalance()).isEqualTo(250000L);
            verify(userAccountRepository, times(1)).updateBalance(userAccount.getUserId(), 250000L);

            // 영속화 레포지토리 호출 검증
            verify(tradeRepository, times(1)).save(any(Trade.class));
            verify(balanceHistoryRepository, times(1)).save(any(BalanceHistory.class));
        }

        @Test
        @DisplayName("실패: 매수 금액보다 예수금이 부족하면 TRADE_INSUFFICIENT_BALANCE 예외가 발생한다.")
        void processOrder_Buy_InsufficientBalance() {
            // given
            TradeRequest request = new TradeRequest(100L, 20, TradeType.BUY); // 20주 매수 = 100만 원 (잔액 50만)
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(stockRepository.findDetailById(request.sdid())).willReturn(stockDetail);

            // when & then
            assertThatThrownBy(() -> tradeService.processOrder(email, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.TRADE_INSUFFICIENT_BALANCE.getMessage());
        }

        @Test
        @DisplayName("성공: 매도 주문이 정상적으로 체결되고 잔액이 증가한다.")
        void processOrder_Sell_Success() {
            // given
            TradeRequest request = new TradeRequest(100L, 3, TradeType.SELL); // 3주 매도 = 15만 원
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(stockRepository.findDetailById(request.sdid())).willReturn(stockDetail);
            // 현재 보유 수량이 5주라고 가정 (보유 5주 - 매도 3주 = 남은 2주 >= 0 이므로 성공)
            given(tradeRepository.findTotalQuantityByStockId(userAccount.getUserId(), stockDetail.getSid())).willReturn(5);

            // when
            TradeResponse response = tradeService.processOrder(email, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.tradeType()).isEqualTo(TradeType.SELL);
            assertThat(response.totalAmount()).isEqualTo(150000L);
            assertThat(response.remainingBalance()).isEqualTo(650000L); // 50만 + 15만 = 65만

            assertThat(userAccount.getBalance()).isEqualTo(650000L);
            verify(userAccountRepository, times(1)).updateBalance(userAccount.getUserId(), 650000L);
            verify(tradeRepository, times(1)).save(any(Trade.class));
        }

        @Test
        @DisplayName("실패: 보유한 주식 수량보다 더 많이 매도하려고 하면 TRADE_INSUFFICIENT_QUANTITY 예외가 발생한다.")
        void processOrder_Sell_InsufficientQuantity() {
            // given
            TradeRequest request = new TradeRequest(100L, 5, TradeType.SELL); // 5주 매도 시도
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(stockRepository.findDetailById(request.sdid())).willReturn(stockDetail);
            // 현재 보유 수량이 2주밖에 없음 (보유 2주 - 매도 5주 = -3이 되므로 실패)
            given(tradeRepository.findTotalQuantityByStockId(userAccount.getUserId(), stockDetail.getSid())).willReturn(2);

            // when & then
            assertThatThrownBy(() -> tradeService.processOrder(email, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.TRADE_INSUFFICIENT_QUANTITY.getMessage());
        }

        @Test
        @DisplayName("실패: 가입되지 않거나 유효하지 않은 이메일인 경우 COMMON_NOT_FOUND 예외가 발생한다.")
        void processOrder_UserNotFound() {
            // given
            TradeRequest request = new TradeRequest(100L, 5, TradeType.BUY);
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> tradeService.processOrder(email, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("거래 이력 조회 검증")
    class GetHistoryTest {

        @Test
        @DisplayName("성공: 사용자의 전체 거래 이력 목록을 반환한다.")
        void getHistoryList_Success() {
            // given
            TradeHistoryResponse historyMock = new TradeHistoryResponse(
                    1L, 10L, "삼성전자", "005930", "반도체", "KOSPI",
                    TradeType.BUY, 50000L, 5, 250000L, 250000L, LocalDateTime.now()
            );
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(tradeRepository.findAllByUserId(userAccount.getUserId())).willReturn(List.of(historyMock));

            // when
            List<TradeHistoryResponse> results = tradeService.getHistoryList(email);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).companyName()).isEqualTo("삼성전자");
            assertThat(results.get(0).tradeType()).isEqualTo(TradeType.BUY);
        }

        @Test
        @DisplayName("성공: 종목 식별자(sid) 기준 내역 목록을 반환한다.")
        void getHistoryListByStockId_Success() {
            // given
            // 1. 리포지토리가 TradeResponse를 직접 반환하므로, 가짜 데이터도 TradeResponse 레코드로 생성합니다.
            TradeResponse tradeResponseMock = new TradeResponse(
                    1L,                // tid
                    10L,               // sid
                    100L,              // sdid
                    TradeType.BUY,     // tradeType
                    50000L,            // tradePrice
                    5,                 // quantity
                    250000L,           // totalAmount
                    250000L,           // remainingBalance
                    LocalDateTime.now()// tradeAt
            );

            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));

            // 2. 이제 리포지토리가 List<TradeResponse>를 반환하므로 타입 유추 에러 없이 깔끔하게 매핑됩니다.
            List<TradeResponse> mockList = List.of(tradeResponseMock);
            given(tradeRepository.findAllByUserIdAndStockId(userAccount.getUserId(), 10L))
                    .willReturn(mockList);

            // when
            List<TradeResponse> results = tradeService.getHistoryListByStockId(email, 10L);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).sid()).isEqualTo(10L);
            assertThat(results.get(0).totalAmount()).isEqualTo(250000L);
        }

        @Test
        @DisplayName("성공: 거래 식별자(tid) 단건 상세 이력을 반환한다.")
        void getHistoryListById_Success() {
            // given
            TradeHistoryResponse historyMock = new TradeHistoryResponse(
                    1L, 10L, "삼성전자", "005930", "반도체", "KOSPI",
                    TradeType.BUY, 50000L, 5, 250000L, 250000L, LocalDateTime.now()
            );
            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
            given(tradeRepository.findByIdAndUserId(1L, userAccount.getUserId())).willReturn(Optional.of(historyMock));

            // when
            TradeHistoryResponse result = tradeService.getHistoryListById(email, 1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.tid()).isEqualTo(1L);
            assertThat(result.companyName()).isEqualTo("삼성전자");
        }
    }
}