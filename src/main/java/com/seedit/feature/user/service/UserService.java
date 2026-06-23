package com.seedit.feature.user.service;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.dto.response.UserProfileResponse;

import java.util.List;

public interface UserService {
    UserAccount addUser(UserAccount userAccount);

    UserAccount getUserByEmail(String email);

    boolean updateUser(UserAccount userAccount);

    boolean updatePassword(String email, String curPassword, String newRawPassword);

    boolean deleteUser(Long userId);

    boolean existsByEmail(String email);

    UserProfileResponse getMyProfile(String email);

}
