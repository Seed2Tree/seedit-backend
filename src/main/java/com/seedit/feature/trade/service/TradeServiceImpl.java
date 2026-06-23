package com.seedit.feature.trade.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.level.domain.PointReason;
import com.seedit.feature.level.service.LevelService;
import com.seedit.feature.portfolio.domain.Portfolio;
import com.seedit.feature.portfolio.repository.PortfolioRepository;
import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.reason.repository.ReasonRepository;
import com.seedit.feature.settlement.domain.Settlement;
import com.seedit.feature.settlement.domain.SettlementStatus;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.settlement.service.SettlementService;
import com.seedit.feature.stock.domain.Stock;
import com.seedit.feature.stock.domain.StockDetail;
import com.seedit.feature.stock.repository.StockRepository;
import com.seedit.feature.trade.domain.Trade;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.trade.dto.request.TradeRequest;
import com.seedit.feature.trade.dto.response.*;

import java.time.LocalDate;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import com.seedit.global.util.BusinessDayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService{

    private final TradeRepository tradeRepository;

    private final StockRepository stockRepository;

    private final UserAccountRepository userAccountRepository;

    private final SettlementService settlementService;

    private final LevelService levelService;

    private final BalanceHistoryRepository balanceHistoryRepository;

    private final PortfolioRepository portfolioRepository;

    private final ReasonRepository reasonRepository;

    @Override
    @Transactional
    public BuyResponse processOrder(String email, TradeRequest request) {
        // 체결 기준 시간을 비즈니스 로직 시작 시점에 고정
        LocalDateTime executionTime = LocalDateTime.now();

        UserAccount userAccount = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자를 찾을 수 없습니다."));

        StockDetail stockDetail = stockRepository.findDetailByTicker(request.ticker());
        if(stockDetail == null){
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND,"종목 시세를 찾을 수 없습니다.");
        }
        Portfolio existing = portfolioRepository.findByUserIdAndSid(userAccount.getUserId(), stockDetail.getSid());


        Long currentPrice = stockDetail.getCurrentPrice();
        Long totalAmount = request.quantity() * currentPrice;
        Long nextBalance = userAccount.getBalance();
        int finalQuantity = request.quantity();


        // 매수 로직 : 유저 잔액 검증 -> 잔액 차감 -> Trade 객체 생성
        nextBalance -= totalAmount;
        if(nextBalance < 0){
            // 잔액 부족
            ErrorCode ec = ErrorCode.TRADE_INSUFFICIENT_BALANCE;
            throw new BusinessException(ec, ec.getMessage());
        }

        if(existing == null){
            // 새로운 주식 종목
            Portfolio portfolio = Portfolio.builder()
                    .userId(userAccount.getUserId())
                    .sid(stockDetail.getSid())
                    .quantity(request.quantity())
                    .avgPrice(currentPrice)
                    .totalAmount(totalAmount)
                    .updatedAt(executionTime)
                    .build();
            portfolioRepository.save(portfolio);
        } else{
            // 기존 주식 추가 매수
            int  newQty   = existing.getQuantity() + request.quantity();
            long newTotal = existing.getTotalAmount() + totalAmount;   // 누적 원가
            existing.setQuantity(newQty);
            existing.setTotalAmount(newTotal);
            existing.setAvgPrice(newTotal / newQty);                   // 가중평균
            existing.setUpdatedAt(executionTime);
            portfolioRepository.updateOneByuserIdAndsid(existing);
        }

        // 유저 정보 업데이트
        userAccount.setBalance(nextBalance);
        userAccountRepository.updateBalance(userAccount.getUserId(),nextBalance);


        Trade trade = Trade.builder()
                .userId(userAccount.getUserId())
                .sid(stockDetail.getSid())
                .sdid(stockDetail.getSdid())
                .tradeType(request.tradeType())
                .tradePrice(stockDetail.getCurrentPrice())
                .quantity(finalQuantity)
                .totalAmount(totalAmount)
                .remainingBalance(nextBalance)
                .tradeAt(executionTime)
                .build();

        BalanceHistory history = BalanceHistory.builder()
                .userId(userAccount.getUserId())
                .amount(totalAmount)
                .currentBalance(nextBalance)
                .reasonType(request.tradeType())
                .createdAt(executionTime)
                .build();

        tradeRepository.save(trade);

        Reason reason = Reason.builder()
                .userId(userAccount.getUserId())
                .tid(trade.getTid())
                .reasonType(TradeType.BUY)
                .reasonDate(executionTime.toLocalDate())
                .reasonTag(request.reasonTag())
                .reasonText(request.reasonText())
                .isVerified(false)
                .isDeleted(false)
                .createdAt(executionTime)
                .build();

        balanceHistoryRepository.save(history);
        reasonRepository.save(reason);
        levelService.addPoint(userAccount.getUserId(), PointReason.TRADE);
        return BuyResponse.from(trade, reason);
    }

    @Override
    @Transactional
    public SellResponse processSell(String email, TradeRequest request) {
        // 체결 기준 시간을 비즈니스 로직 시작 시점에 고정
        LocalDateTime executionTime = LocalDateTime.now();

        UserAccount userAccount = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자를 찾을 수 없습니다."));

        StockDetail stockDetail = stockRepository.findDetailByTicker(request.ticker());
        if(stockDetail == null){
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND,"종목 시세를 찾을 수 없습니다.");
        }
        Portfolio existing = portfolioRepository.findByUserIdAndSid(userAccount.getUserId(), stockDetail.getSid());


        Long currentPrice = stockDetail.getCurrentPrice();
        Long totalAmount = request.quantity() * currentPrice;
        Long nextBalance = userAccount.getBalance();
        int finalQuantity = request.quantity();


       // 매도 로직 : 유저 주식 보유 수량 검증 -> 잔액 증가 -> Trade 객체 생성
        // 매도 시 수량은 양수로 받기
        if (existing == null || existing.getQuantity() < request.quantity()) {
            ErrorCode ec = ErrorCode.TRADE_INSUFFICIENT_QUANTITY;
            throw new BusinessException(ec, ec.getMessage());
        }
        int remainingQuantity = existing.getQuantity() - request.quantity();

        // 잔액 증가
//        nextBalance += totalAmount;
        finalQuantity = -request.quantity();

        long newTotal = existing.getAvgPrice() * remainingQuantity;   // 평단은 유지, 남은 수량만큼 원가
        existing.setQuantity(remainingQuantity);
        existing.setTotalAmount(newTotal);
        existing.setUpdatedAt(executionTime);
        portfolioRepository.updateOneByuserIdAndsid(existing);

        Trade trade = Trade.builder()
                .userId(userAccount.getUserId())
                .sid(stockDetail.getSid())
                .sdid(stockDetail.getSdid())
                .tradeType(request.tradeType())
                .tradePrice(stockDetail.getCurrentPrice())
                .quantity(finalQuantity)
                .totalAmount(totalAmount)
                .remainingBalance(nextBalance)
                .tradeAt(executionTime)
                .build();

        tradeRepository.save(trade);

        // 유저 정보 업데이트
//        userAccount.setBalance(nextBalance);
//        userAccountRepository.updateBalance(userAccount.getUserId(),nextBalance);
        LocalDate today = executionTime.toLocalDate();
        settlementService.reserve(userAccount.getUserId(), trade.getTid(), totalAmount, today);

        //        BalanceHistory history = BalanceHistory.builder()
//                .userId(userAccount.getUserId())
//                .amount(totalAmount)
//                .currentBalance(nextBalance)
//                .reasonType(request.tradeType())
//                .createdAt(executionTime)
//                .build();
//        balanceHistoryRepository.save(history);


        Reason reason = Reason.builder()
                .userId(userAccount.getUserId())
                .tid(trade.getTid())
                .reasonType(TradeType.SELL)
                .reasonDate(executionTime.toLocalDate())
                .reasonTag(request.reasonTag())
                .reasonText(request.reasonText())
                .isVerified(false)
                .isDeleted(false)
                .createdAt(executionTime)
                .build();
        reasonRepository.save(reason);

        levelService.addPoint(userAccount.getUserId(), PointReason.TRADE);
        return SellResponse.from(trade, reason);
    }

    @Override
    public BuyPrepareResponse getBuystock(String email, String ticker) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        StockDetail stockDetail = stockRepository.findDetailByTicker(ticker);
        if(stockDetail == null){
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND);
        }
        return BuyPrepareResponse.from(user.getBalance(),stockDetail);
    }

    @Override
    public SellPrepareResponse getSellstock(String email, String ticker) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        StockDetail stockDetail = stockRepository.findDetailByTicker(ticker);
        if(stockDetail == null){
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND);
        }
        Portfolio portfolio = portfolioRepository.findByUserIdAndSid(user.getUserId(), stockDetail.getSid());
        List<Reason> reasons = reasonRepository.findByUserIdAndSid(user.getUserId(), stockDetail.getSid());

        return SellPrepareResponse.from(portfolio, reasons, stockDetail);
    }

    @Override
    public List<TradeHistoryResponse> getHistoryList(String email, int month) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        Long userId = user.getUserId();

        List<TradeHistoryResponse> responses = tradeRepository.findAllByUserId(userId,month);

        return responses;
    }

    @Override
    public List<TradeResponse> getHistoryListByStockId(String email, Long sid) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        Long userId = user.getUserId();
        List<TradeResponse> responses = tradeRepository.findAllByUserIdAndStockId(userId, sid);

        return responses;
    }

    @Override
    public TradeHistoryResponse getHistoryListById(String email, Long tid) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        Long userId = user.getUserId();

        TradeHistoryResponse response = tradeRepository.findByIdAndUserId(userId, tid)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자 혹은 거래 내역을 찾을 수 없습니다."));

        return response;
    }

    @Override
    public List<TradeHistoryResponse> getHistoryListByDate(String email, LocalDate date) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        return tradeRepository.findAllByUserIdAndDate(user.getUserId(), date);
    }

    @Override
    public List<TradeCalendarEntry> getTradeCalendar(String email, int year, int month) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        return tradeRepository.findCalendarByUserIdAndMonth(user.getUserId(), year, month);
    }

}
