package com.seedit.feature.reason.repository;

import com.seedit.feature.reason.domain.Reason;
import com.seedit.feature.reason.dto.response.ReasonResponse;
import com.seedit.feature.trade.dto.response.VerifiedReasonResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;
@Mapper
public interface ReasonRepository {
    int save(Reason reason); // 가설 설정/복기 저장

    List<ReasonResponse> findAllByUserIdAndDate(@Param("userId") Long userId, @Param("reasonDate") Date reasonDate); // 사용자별, 날짜별 가설 전체 조회

    Optional<Reason> findByUserIdAndId(@Param("userId") Long userId, @Param("rid") Long rid); // 가설 내역 단건 조회

    List<Reason> findByUserIdAndSid(@Param("userId") Long userId, @Param("sid") Long sid); // 가설 검증이 안된 주식에 대한 단건 조회

    int countByUserId(@Param("userId") Long userId); // 가설 작성 횟수 조회

    int deleteByUserId(@Param("userId") Long userId);

    int updateVerified(@Param("userId") Long userId,
                       @Param("rid") Long rid,
                       @Param("sellTid") Long sellTid);

    List<VerifiedReasonResponse> findByUserIdAndVerifiedTid(
            @Param("userId") Long userId, @Param("tid") Long tid);
}
