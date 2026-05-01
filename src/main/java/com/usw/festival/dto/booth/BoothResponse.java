package com.usw.festival.dto.booth;

import com.usw.festival.entity.Booth;
import com.usw.festival.entity.College;

public record BoothResponse(
        Long boothId,
        String name,
        String imageUrl,
        College college,
        String collegeLabel
) {
    public static BoothResponse from(Booth booth) {
        College college = booth.getCollege();
        return new BoothResponse(
                booth.getId(),
                booth.getName(),
                booth.getImageUrl(),
                college,
                college == null ? null : college.getDisplayName()
        );
    }
}
