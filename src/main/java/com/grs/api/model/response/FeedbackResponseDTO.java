package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 10-Apr-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedbackResponseDTO {
    private String title;
    private Float rating;
    private String comments;
}
