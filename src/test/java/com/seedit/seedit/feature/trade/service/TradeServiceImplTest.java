//package com.seedit.seedit.feature.trade.service;
//
//import com.seedit.feature.balance.domain.BalanceHistory;
//import com.seedit.feature.balance.repository.BalanceHistoryRepository;
//import com.seedit.feature.portfolio.repository.PortfolioRepository;
//import com.seedit.feature.reason.repository.ReasonRepository;
//import com.seedit.feature.stock.domain.StockDetail;
//import com.seedit.feature.stock.repository.StockRepository;
//import com.seedit.feature.trade.domain.Trade;
//import com.seedit.feature.trade.domain.TradeType;
//import com.seedit.feature.trade.dto.request.TradeRequest;
//import com.seedit.feature.trade.dto.response.TradeHistoryResponse;
//import com.seedit.feature.trade.dto.response.TradeResponse;
//import com.seedit.feature.trade.repository.TradeRepository;
//import com.seedit.feature.trade.service.TradeServiceImpl;
//import com.seedit.feature.user.domain.UserAccount;
//import com.seedit.feature.user.repository.UserAccountRepository;
//import com.seedit.global.error.BusinessException;
//import com.seedit.global.error.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//@ExtendWith(MockitoExtension.class)
//@DisplayName("TradeService 비즈니스 로직 단위 테스트")
//class TradeServiceImplTest {
//
//    @InjectMocks private TradeServiceImpl tradeService;
//
//    @Mock private TradeRepository tradeRepository;
//    @Mock private StockRepository stockRepository;
//    @Mock private UserAccountRepository userAccountRepository;
//    @Mock private BalanceHistoryRepository balanceHistoryRepository;
//    @Mock private PortfolioRepository portfolioRepository;   // 추가
//    @Mock private ReasonRepository reasonRepository;         // 추가
//
//    private final String email = "test@seedit.com";
//    private UserAccount userAccount;
//    private StockDetail stockDetail;
//
//    @BeforeEach
//    void setUp() {
//        userAccount = new UserAccount();
//        userAccount.setUserId(1L);
//        userAccount.setBalance(500000L);
//
//        stockDetail = new StockDetail();
//        stockDetail.setSid(10L);
//        stockDetail.setSdid(100L);
//        stockDetail.setCurrentPrice(50000L);
//    }
//
//    @Nested
//    @DisplayName("매수 (processOrder)")
//    class BuyTest {
//
//        @Test
//        @DisplayName("성공: 신규 종목 매수 시 잔액 차감 + 포트폴리오 신규 저장 + 가설 저장")
//        void buy_NewHolding_Success() {
//            TradeRequest request = new TradeRequest(100L, 5, TradeType.BUY, "실적", "PER 저평가");
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//            given(portfolioRepository.findByUserIdAndSid(1L, 10L)).willReturn(null); // 신규
//
//            BuyResponse response = tradeService.processOrder(email, request);
//
//            assertThat(response.tradeType()).isEqualTo(TradeType.BUY);
//            assertThat(response.quantity()).isEqualTo(5);
//            assertThat(response.reasonTag()).isEqualTo("실적");
//            assertThat(userAccount.getBalance()).isEqualTo(250000L); // 50만 - 25만
//
//            verify(userAccountRepository).updateBalance(1L, 250000L);
//            verify(portfolioRepository).save(any(Portfolio.class));   // 신규라 save
//            verify(portfolioRepository, never()).updateOneByuserIdAndsid(any());
//            verify(tradeRepository).save(any(Trade.class));
//            verify(balanceHistoryRepository).save(any(BalanceHistory.class));
//            verify(reasonRepository).save(any(Reason.class));
//        }
//
//        @Test
//        @DisplayName("성공: 기존 보유 종목 추가 매수 시 가중평균 평단가로 갱신된다")
//        void buy_AddHolding_WeightedAvg() {
//            TradeRequest request = new TradeRequest(100L, 5, TradeType.BUY, "추가매수", "분할매수");
//            // 기존: 5주, 평단 40000, 원가 200000
//            Portfolio holding = Portfolio.builder()
//                    .userId(1L).sid(10L).quantity(5).avgPrice(40000L).totalAmount(200000L).build();
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//            given(portfolioRepository.findByUserIdAndSid(1L, 10L)).willReturn(holding);
//
//            tradeService.processOrder(email, request);
//
//            // newQty=10, newTotal=200000+250000=450000, avg=45000
//            assertThat(holding.getQuantity()).isEqualTo(10);
//            assertThat(holding.getTotalAmount()).isEqualTo(450000L);
//            assertThat(holding.getAvgPrice()).isEqualTo(45000L);
//            verify(portfolioRepository).updateOneByuserIdAndsid(holding);
//            verify(portfolioRepository, never()).save(any());
//        }
//
//        @Test
//        @DisplayName("실패: 잔액 부족 시 TRADE_INSUFFICIENT_BALANCE")
//        void buy_InsufficientBalance() {
//            TradeRequest request = new TradeRequest(100L, 20, TradeType.BUY, "t", "x"); // 100만 > 50만
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//
//            assertThatThrownBy(() -> tradeService.processOrder(email, request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasMessageContaining(ErrorCode.TRADE_INSUFFICIENT_BALANCE.getMessage());
//            // 롤백 의도: 영속화가 일어나지 않아야 함
//            verify(tradeRepository, never()).save(any());
//            verify(portfolioRepository, never()).save(any());
//        }
//    }
//
//    @Nested
//    @DisplayName("매도 (processSell)")
//    class SellTest {
//
//        @Test
//        @DisplayName("성공: 매도 시 잔액 증가 + 보유수량 차감 + 평단가 유지")
//        void sell_Success() {
//            TradeRequest request = new TradeRequest(100L, 3, TradeType.SELL, "목표가", "차익실현");
//            Portfolio holding = Portfolio.builder()
//                    .userId(1L).sid(10L).quantity(5).avgPrice(40000L).totalAmount(200000L).build();
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//            given(portfolioRepository.findByUserIdAndSid(1L, 10L)).willReturn(holding);
//
//            SellResponse response = tradeService.processSell(email, request);
//
//            assertThat(response.tradeType()).isEqualTo(TradeType.SELL);
//            assertThat(userAccount.getBalance()).isEqualTo(650000L); // 50만 + 15만
//            // 평단 유지, 수량 차감, 원가 = 평단 * 잔여수량
//            assertThat(holding.getQuantity()).isEqualTo(2);
//            assertThat(holding.getAvgPrice()).isEqualTo(40000L);
//            assertThat(holding.getTotalAmount()).isEqualTo(80000L); // 40000 * 2
//            verify(userAccountRepository).updateBalance(1L, 650000L);
//            verify(portfolioRepository).updateOneByuserIdAndsid(holding);
//            verify(reasonRepository).save(any(Reason.class));
//        }
//
//        @Test
//        @DisplayName("실패: 보유 수량 초과 매도 시 TRADE_INSUFFICIENT_QUANTITY")
//        void sell_InsufficientQuantity() {
//            TradeRequest request = new TradeRequest(100L, 5, TradeType.SELL, "t", "x");
//            Portfolio holding = Portfolio.builder()
//                    .userId(1L).sid(10L).quantity(2).avgPrice(40000L).totalAmount(80000L).build();
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//            given(portfolioRepository.findByUserIdAndSid(1L, 10L)).willReturn(holding);
//
//            assertThatThrownBy(() -> tradeService.processSell(email, request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasMessageContaining(ErrorCode.TRADE_INSUFFICIENT_QUANTITY.getMessage());
//            verify(tradeRepository, never()).save(any());
//        }
//
//        @Test
//        @DisplayName("실패: 보유한 적 없는 종목 매도 시 TRADE_INSUFFICIENT_QUANTITY")
//        void sell_NoHolding() {
//            TradeRequest request = new TradeRequest(100L, 1, TradeType.SELL, "t", "x");
//            given(userAccountRepository.findUserByEmail(email)).willReturn(Optional.of(userAccount));
//            given(stockRepository.findDetailById(100L)).willReturn(stockDetail);
//            given(portfolioRepository.findByUserIdAndSid(1L, 10L)).willReturn(null);
//
//            assertThatThrownBy(() -> tradeService.processSell(email, request))
//                    .isInstanceOf(BusinessException.class)
//                    .hasMessageContaining(ErrorCode.TRADE_INSUFFICIENT_QUANTITY.getMessage());
//        }
//    }
//}