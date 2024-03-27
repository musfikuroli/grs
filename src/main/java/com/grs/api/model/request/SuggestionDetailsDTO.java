package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;

/**
 * Created by HP on 1/8/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestionDetailsDTO {
    @NotEmpty
    private String name;
    @Email
    private String email;
    private String phone;
    private Long officeId;
    private String officeName;
    private String officeServiceId;
    private String officeServiceName;
    private String suggestionType;
    private String typeOfSuggestionForImprovement;
    private String typeOfFeedback;
    private String description;
    private String suggestion;
    private String effectTowardsSolution;
    private String submissionDate;
}
