package com.seedit.feature.study.repository;

import com.seedit.feature.study.domain.InvestmentStudy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StudyRepository {

    List<InvestmentStudy> findAll();

    List<InvestmentStudy> findByCategory(@Param("category") String category);

    Optional<InvestmentStudy> findById(@Param("isid") Long isid);

    // 즐겨찾기
    void insertBookmark(@Param("userId") Long userId, @Param("isid") Long isid);

    void deleteBookmark(@Param("userId") Long userId, @Param("isid") Long isid);

    int countBookmark(@Param("userId") Long userId, @Param("isid") Long isid);

    List<Long> findBookmarkIdsByUserId(@Param("userId") Long userId);

    List<InvestmentStudy> findBookmarkedByUserId(@Param("userId") Long userId);
}
