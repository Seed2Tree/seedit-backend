package com.seedit.feature.diary.repository;

import com.seedit.feature.diary.domain.Diary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DiaryRepository {

    List<Diary> findAllByUserId(@Param("userId") Long userId);

    List<LocalDate> findDatesByUserIdAndMonth(@Param("userId") Long userId,
                                              @Param("year") int year,
                                              @Param("month") int month);

    Optional<Diary> findByUserIdAndDate(@Param("userId") Long userId,
                                        @Param("diaryDate") LocalDate diaryDate);

    int countByUserIdAndDate(@Param("userId") Long userId,
                             @Param("diaryDate") LocalDate diaryDate);

    int countByUserId(@Param("userId") Long userId);

    int insert(@Param("userId") Long userId,
               @Param("diaryDate") LocalDate diaryDate,
               @Param("content") String content);

    int update(@Param("did") Long did,
               @Param("userId") Long userId,
               @Param("content") String content);

    int delete(@Param("did") Long did,
               @Param("userId") Long userId);

    Optional<Diary> findByDidAndUserId(@Param("did") Long did,
                                       @Param("userId") Long userId);

    int updateAiFeedback(@Param("did") Long did,
                         @Param("userId") Long userId,
                         @Param("aiFeedback") String aiFeedback);
}
