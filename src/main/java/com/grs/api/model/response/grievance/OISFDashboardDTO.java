package com.grs.api.model.response.grievance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OISFDashboardDTO {
    private String Title;
    private String Count;
    private String icon;
    private int DisplayOrder;
    private String redirectURL;
}
