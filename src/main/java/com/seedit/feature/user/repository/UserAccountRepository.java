package com.seedit.feature.user.repository;

import com.seedit.feature.user.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserAccountRepository {
    int saveUser(UserAccount userAccount);

    List<UserAccount> findAllUsers();

    Optional<UserAccount> findUserById(Long userId);

    Optional<UserAccount> findUserByEmail(String email);

    int countByEmail(String email);

    int updateUser(UserAccount userAccount);

    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String encodedPassword);

    int updateBalance(@Param("userId") Long userId, @Param("remainingBalance") Long remainingBalance);

    int resetTotalInvested(@Param("userId") Long userId);

    int deleteUser(Long userId);

}
