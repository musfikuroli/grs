package com.grs.api.model.request;

import com.grs.core.domain.EffectsTowardsSolution;
import com.grs.core.domain.ImprovementSuggestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by Acer on 26-Sep-17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SuggestionRequestDTO {
    @NotEmpty
    private String name;
    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    private String phone;
    @NotEmpty
    private Long officeId;
    private Long officeServiceId;
    private String officeServiceName;
    private ImprovementSuggestion typeOfSuggestionForImprovement;
    private String description;
    private String suggestion;
    private EffectsTowardsSolution effectTowardsSolution;
}
