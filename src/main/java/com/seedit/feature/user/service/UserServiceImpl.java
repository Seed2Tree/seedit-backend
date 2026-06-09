package com.seedit.feature.user.service;

import com.seedit.feature.user.domain.UserAccount;
import com.seedit.feature.user.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean addUser(UserAccount userAccount) {
        return userAccountRepository.insertUser(userAccount) == 1;
    }

    @Override
    public List<UserAccount> getAllUsers() {
        return userAccountRepository.selectAllUsers();
    }

    @Override
    public UserAccount getUserById(int userId) {
        return userAccountRepository.selectUserById(userId);
    }

    @Override
    public UserAccount getUserByEmail(String email) {
        return userAccountRepository.selectUserByEmail(email);
    }

    @Override
    public boolean updateUser(UserAccount userAccount) {
        return userAccountRepository.updateUser(userAccount) == 1;
    }

    @Override
    public boolean updatePassword(String email, String newRawPassword) {
        UserAccount user = userAccountRepository.selectUserByEmail(email);
        if(user == null){
            return false;
        }
        String encodedPassword = passwordEncoder.encode(newRawPassword);

        user.setPasswordHash(encodedPassword);
        return userAccountRepository.updateUser(user) > 0;
    }

    @Override
    public boolean deleteUser(int userId) {
        return userAccountRepository.deleteUser(userId) == 1;
    }
}
