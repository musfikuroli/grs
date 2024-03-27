package com.grs.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 3/11/2018.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrsRoleToSmsDTO {
    private Long id;
    private Long smsTemplateId;
    private String grsRole;
    private Boolean status;
}
