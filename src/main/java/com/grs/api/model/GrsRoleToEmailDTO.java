package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 3/11/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GrsRoleToEmailDTO {
    private Long id;
    private Long emailTemplateId;
    private String grsRole;
    private Boolean status;
}
