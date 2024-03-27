package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by HP on 2/7/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailTemplateDTO {
    private Long id;
    private Long actionToRole;
    private String emailTemplateName;
    private String emailTemplateBodyEng;
    private String emailTemplateBodyBng;
    private String emailTemplateSubjectEng;
    private String emailTemplateSubjectBng;
    private Boolean status;
    private String language;
    private List<String> recipient;
}
