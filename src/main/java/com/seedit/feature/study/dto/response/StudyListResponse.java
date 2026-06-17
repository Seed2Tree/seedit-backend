package com.seedit.feature.study.dto.response;

import com.seedit.feature.study.domain.InvestmentStudy;

public record StudyListResponse(
        Long isid,
        String title,
        String youtubeUrl,
        String category,
        String thumbnail
) {
    public static StudyListResponse from(InvestmentStudy s) {
        return new StudyListResponse(s.getIsid(), s.getTitle(), s.getYoutubeUrl(), s.getCategory(), s.getThumbnail());
    }
}
