package com.seedit.feature.study.dto.response;

import com.seedit.feature.study.domain.InvestmentStudy;

public record StudyDetailResponse(
        Long isid,
        String title,
        String youtubeUrl,
        String description,
        String category,
        String thumbnail
) {
    public static StudyDetailResponse from(InvestmentStudy s) {
        return new StudyDetailResponse(s.getIsid(), s.getTitle(), s.getYoutubeUrl(), s.getDescription(), s.getCategory(), s.getThumbnail());
    }
}
