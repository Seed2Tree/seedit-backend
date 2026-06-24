package com.seedit.feature.user.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.dto.response.BalanceHistoryResponse;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.diary.repository.DiaryRepository;
import com.seedit.feature.level.domain.UserLevel;
import com.seedit.feature.level.repository.LevelDefinitionRepository;
import com.seedit.feature.level.repository.UserLevelRepository;
import com.seedit.feature.level.service.LevelService;
import com.seedit.feature.portfolio.dto.response.PortfolioSummaryResponse;
import com.seedit.feature.portfolio.repository.PortfolioRepository;
import com.seedit.feature.portfolio.service.PortfolioService;
import com.seedit.feature.reason.repository.ReasonRepository;
import com.seedit.feature.settlement.repository.SettlementRepository;
import com.seedit.feature.settlement.service.SettlementService;
import com.seedit.feature.study.repository.StudyRepository;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.trade.dto.response.TradeHistoryResponse;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.dto.response.UserProfileResponse;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.feature.watchlist.repository.WatchlistRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLevelRepository userLevelRepository;
    private final LevelDefinitionRepository levelDefinitionRepository;

    private final BalanceHistoryRepository balanceHistoryRepository;
    private final TradeRepository tradeRepository;
    private final DiaryRepository diaryRepository;
    private final WatchlistRepository watchlistRepository;
    private final StudyRepository studyRepository;
    private final LevelService levelService;
    private final PortfolioService portfolioService;
    private final PortfolioRepository portfolioRepository;
    private final SettlementRepository settlementRepository;
    private final ReasonRepository reasonRepository;

    private static final Long INITIAL_BALANCE = 5000000L;
    // 회원 가입 로직
    @Override
    @Transactional
    public UserAccount addUser(UserAccount userAccount) {
        userAccount.setBalance(INITIAL_BALANCE);
        int userResult = userAccountRepository.saveUser(userAccount);
        if(userResult != 1){
            throw new BusinessException(ErrorCode.COMMON_INTERNAL);
        }

        userLevelRepository.insertDefaultLevel(userAccount.getUserId());

        BalanceHistory initHistory = BalanceHistory.builder()
                .userId(userAccount.getUserId())
                .amount(INITIAL_BALANCE)
                .currentBalance(INITIAL_BALANCE)
                .reasonType(TradeType.INIT)
                .build();

        balanceHistoryRepository.save(initHistory);
        return userAccount;
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        return userAccountRepository.findUserByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "존재하지 않는 회원입니다. email="+email));
    }

    @Override
    public boolean updateUser(UserAccount userAccount) {
        return userAccountRepository.updateUser(userAccount) == 1;
    }

    @Override
    public boolean updatePassword(String email, String curPassword, String newRawPassword) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND,"존재하지 않는 회원입니다. email="+email));

        if(!passwordEncoder.matches(curPassword, user.getPasswordHash())){
            throw new BusinessException(ErrorCode.AUTH_INVALID_PASSWORD,"현재 비밀번호가 일치하지 않습니다.");
        }

        if(passwordEncoder.matches(newRawPassword, user.getPasswordHash())){
            throw new BusinessException(ErrorCode.COMMON_VALIDATION,"기존 비밀번호와 다른 비밀번호를 입력해주세요.");
        }

        String encodedPassword = passwordEncoder.encode(newRawPassword);
        return userAccountRepository.updatePassword(user.getUserId(),encodedPassword) == 1;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return userAccountRepository.deleteUser(userId) == 1;
    }

    // 회원 초기화 로직
    @Override
    @Transactional
    public UserAccount resetUser(String email) {
        UserAccount userAccount = userAccountRepository.findUserByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"해당 계정을 찾을 수 없습니다."));
        Long userId = userAccount.getUserId();
        // 정산 레포 초기화
        settlementRepository.deleteByUserId(userId);

        // 가설 초기화
        reasonRepository.deleteByUserId(userId);

        // 거래 초기화
        tradeRepository.deleteByUserId(userId);

        // 포트폴리오 초기화
        portfolioRepository.deleteByUserId(userId);

        // 거래 일지 초기화
        diaryRepository.deleteByUserId(userId);

        // 관심 종목 초기화
        watchlistRepository.deleteByUserId(userId);

        // 경제 유튜브 북마크 초기화
        studyRepository.deleteByUserId(userId);


        userAccountRepository.updateBalance(userId,INITIAL_BALANCE);
        userAccountRepository.resetTotalInvested(userId);

        // 레벨/포인트 초기화
        userLevelRepository.updatePointAndLevel(userId,0,1);

        BalanceHistory initHistory = BalanceHistory.builder()
                .userId(userId)
                .amount(INITIAL_BALANCE)
                .currentBalance(INITIAL_BALANCE)
                .reasonType(TradeType.INIT)
                .build();

        balanceHistoryRepository.save(initHistory);
        return userAccount;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userAccountRepository.countByEmail(email) > 0;
    }

    // 내 정보 조회
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {
        UserAccount userAccount = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자를 찾을 수 없습니다."));

        UserLevel userLevel = userLevelRepository.selectByUserId(userAccount.getUserId())
                .orElseThrow(()-> new BusinessException(ErrorCode.COMMON_NOT_FOUND,"사용자의 레벨 정보가 존재하지 않습니다."));

        String levelName = levelDefinitionRepository.findLevelNameByLevel(userLevel.getLevel());
        int nextLevelPoint = levelService.getNextLevelPoint(userLevel.getLevel());
        var levelInfo = new UserProfileResponse.LevelInfo(
                userLevel.getLevel(), levelName, userLevel.getPoint(), nextLevelPoint);
        PortfolioSummaryResponse psr = portfolioService.findAllByUserId(userAccount.getUserId(),userAccount.getBalance());
        userAccount.setTotalInvested(psr.totalEval());

        int transactionCount = tradeRepository.countByUserId(userAccount.getUserId());
        int diaryCount = diaryRepository.countByUserId(userAccount.getUserId());
        int watchlistCount = watchlistRepository.countStockNumByUserId(userAccount.getUserId());
        int studyBookmarkCount = studyRepository.countAll(userAccount.getUserId());

        UserProfileResponse.ActivityInfo activityInfo = new UserProfileResponse.ActivityInfo(transactionCount, diaryCount, watchlistCount, studyBookmarkCount);

        return UserProfileResponse.from(userAccount, levelInfo, activityInfo);
    }


}
