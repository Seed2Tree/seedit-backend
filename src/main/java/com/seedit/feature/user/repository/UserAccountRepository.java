package com.seedit.feature.user.repository;

import com.seedit.feature.user.domain.UserAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAccountRepository {
    int insertUser(UserAccount userAccount);

    List<UserAccount> selectAllUsers();

    UserAccount selectUserById(int userId);

    UserAccount selectUserByEmail(String email);

    int updateUser(UserAccount userAccount);

    int deleteUser(int userId);

}
