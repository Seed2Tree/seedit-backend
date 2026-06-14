package com.seedit.feature.balance.repository;

import com.seedit.feature.balance.domain.BalanceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Mapper
public interface BalanceHistoryRepository {
    int save(BalanceHistory history);
    Optional<BalanceHistory> findById(Long bhid);
    List<BalanceHistory> findByUserId(Long userId);
}
