package com.seedit.feature.report.repository;

import com.seedit.feature.report.domain.AiReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiReportRepository {

    /** 단건 조회 (없으면 null) */
    AiReport findOne(@Param("stockCode") String stockCode,
                     @Param("bsnsYear") Integer bsnsYear,
                     @Param("reprtCode") String reprtCode);

    /** 종목의 저장된 분기 목록(최신순) — 프론트 필터용 */
    List<AiReport> findPeriods(@Param("stockCode") String stockCode);

    /** 저장/갱신 (uq_report 충돌 시 content/model/updated_at 갱신) */
    void upsert(AiReport report);
}