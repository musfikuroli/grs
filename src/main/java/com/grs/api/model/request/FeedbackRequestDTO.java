package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 05-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackRequestDTO {
    private Long grievanceId;
    private Float rating;
    private String userComments;
}
