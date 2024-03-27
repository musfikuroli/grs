package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 1/31/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class EducationDTO {
    private Long Id;
    private String educationBangla;
    private String educationEnglish;
    private Boolean status;
}

