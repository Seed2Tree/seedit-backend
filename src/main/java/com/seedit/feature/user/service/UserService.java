package com.seedit.feature.user.service;

import com.seedit.feature.user.domain.UserAccount;

import java.util.List;

public interface UserService {
    boolean addUser(UserAccount userAccount);

    List<UserAccount> getAllUsers();

    UserAccount getUserById(int userId);

    UserAccount getUserByEmail(String email);

    boolean updateUser(UserAccount userAccount);

    boolean updatePassword(String email, String newRawPassword);

    boolean deleteUser(int userId);

}
