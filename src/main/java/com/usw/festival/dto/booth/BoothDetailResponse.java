package com.usw.festival.dto.booth;

import com.usw.festival.entity.Booth;
import com.usw.festival.entity.College;

public record BoothDetailResponse(
        Long boothId,
        String name,
        String description,
        String imageUrl,
        String notice,
        College college,
        String collegeLabel
) {
    public static BoothDetailResponse from(Booth booth) {
        College college = booth.getCollege();
        return new BoothDetailResponse(
                booth.getId(),
                booth.getName(),
                booth.getDescription(),
                booth.getImageUrl(),
                booth.getNotice(),
                college,
                college == null ? null : college.getDisplayName()
        );
    }
}
