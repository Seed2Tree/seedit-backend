package com.seedit.feature.trade.service;

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
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
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

    private final BalanceHistoryRepository balanceHistoryRepository;

    @Override
    @Transactional
    public TradeResponse processOrder(String email, TradeRequest request) {
        UserAccount userAccount = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자를 찾을 수 없습니다."));

        StockDetail stockDetail = stockRepository.findDetailById(request.sdid());
        if(stockDetail == null){
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND,"종목 시세를 찾을 수 없습니다.");
        }
        Long currentPrice = stockDetail.getCurrentPrice();
        Long totalAmount = request.quantity() * currentPrice;
        Long nextBalance = userAccount.getBalance();
        int finalQuantity = request.quantity();


        if(request.tradeType() == TradeType.BUY){
            // 매수 로직 : 유저 잔액 검증 -> 잔액 차감 -> Trade 객체 생성
            nextBalance -= totalAmount;
            if(nextBalance < 0){
                // 잔액 부족
                ErrorCode ec = ErrorCode.TRADE_INSUFFICIENT_BALANCE;
                throw new BusinessException(ec, ec.getMessage());
            }

        } else if ((request.tradeType() == TradeType.SELL)){
            // 매도 로직 : 유저 주식 보유 수량 검증 -> 잔액 증가 -> Trade 객체 생성
            // 매도 시 수량은 양수로 받기
            int remainingQuantity = tradeRepository.findTotalQuantityByStockId(userAccount.getUserId(),stockDetail.getSid()) - request.quantity();

            if(remainingQuantity < 0){
                // 주식 수량 부족
                ErrorCode ec = ErrorCode.TRADE_INSUFFICIENT_QUANTITY;
                throw new BusinessException(ec, ec.getMessage());
            }

            // 잔액 증가
            nextBalance += totalAmount;
            finalQuantity = -request.quantity();
        }

        // 유저 정보 업데이트
        userAccount.setBalance(nextBalance);
        userAccountRepository.updateBalance(userAccount.getUserId(),nextBalance);


        // 체결 기준 시간을 비즈니스 로직 마친 시점에 고정
        LocalDateTime executionTime = LocalDateTime.now();
        Trade trade = Trade.builder()
                .userId(userAccount.getUserId())
                .sid(stockDetail.getSid())
                .sdid(request.sdid())
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
        balanceHistoryRepository.save(history);

        return TradeResponse.from(trade);
    }

    @Override
    public List<TradeHistoryResponse> getHistoryList(String email) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        Long userId = user.getUserId();

        List<TradeHistoryResponse> responses = tradeRepository.findAllByUserId(userId);

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


}
