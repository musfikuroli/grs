package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 26-Sep-17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestionResponseDTO {
    private Long id;
    private Long officeId;
    private String officeName;
    private String serviceName;
    private String subject;
    private String description;
    private String suggestion;
    private String possibleEffect;
    private String submissionDate;
}
