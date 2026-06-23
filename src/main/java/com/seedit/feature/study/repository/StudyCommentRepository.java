package com.seedit.feature.study.repository;

import com.seedit.feature.study.dto.response.StudyCommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StudyCommentRepository {
    List<StudyCommentResponse> findByIsid(@Param("isid") Long isid, @Param("userId") Long userId);
    void insert(@Param("isid") Long isid, @Param("userId") Long userId, @Param("content") String content);
    Optional<Long> findOwnerUserId(@Param("scid") Long scid);
    void update(@Param("scid") Long scid, @Param("content") String content);
    void delete(@Param("scid") Long scid);
}
