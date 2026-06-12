package com.seedit.feature.user.service;

import com.seedit.feature.balance.domain.BalanceHistory;
import com.seedit.feature.balance.dto.response.BalanceHistoryResponse;
import com.seedit.feature.balance.repository.BalanceHistoryRepository;
import com.seedit.feature.level.domain.UserLevel;
import com.seedit.feature.level.repository.LevelDefinitionRepository;
import com.seedit.feature.level.repository.UserLevelRepository;
import com.seedit.feature.trade.domain.TradeType;
import com.seedit.feature.trade.repository.TradeRepository;
import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.dto.response.UserProfileResponse;
import com.seedit.feature.user.repository.UserAccountRepository;
import com.seedit.global.error.BusinessException;
import com.seedit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
//    private final TradeRepository tradeRepository;
//    private final DiaryRepository diaryRepository;
//    private final WatchlistRepository watchlistRepository;
//    private final StudyBookmarkRepository studyBookmarkRepository;

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
    public boolean updatePassword(String email, String newRawPassword) {
        UserAccount user = userAccountRepository.findUserByEmail(email)
                .orElseThrow(()->new BusinessException(ErrorCode.COMMON_NOT_FOUND,"존재하지 않는 회원입니다. email="+email));

        String encodedPassword = passwordEncoder.encode(newRawPassword);

        return userAccountRepository.updatePassword(user.getUserId(),encodedPassword) == 1;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return userAccountRepository.deleteUser(userId) == 1;
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

//        int transactionCount = tradeRepository.countByUserId(userAccount.getUserId());
//        int diaryCount = diaryRepository.countByUserId(userAccount.getUserId());
//        int watchlistCount = watchlistRepository.countByUserId(userAccount.getUserId());
//        int studyBookmarkCount = studyBookmarkRepository.countByUserId(userAccount.getUserId());

        UserProfileResponse.LevelInfo levelInfo = new UserProfileResponse.LevelInfo(userLevel.getLevel(), levelName,userLevel.getPoint());
//        UserProfileResponse.ActivityInfo activityInfo = new UserProfileResponse.ActivityInfo(transactionCount, diaryCount, watchlistCount, studyBookmarkCount);
        UserProfileResponse.ActivityInfo activityInfo = new UserProfileResponse.ActivityInfo(0,0,0,0);

        return UserProfileResponse.from(userAccount, levelInfo, activityInfo);
    }


}
