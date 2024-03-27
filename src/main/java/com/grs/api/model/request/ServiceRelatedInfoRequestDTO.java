package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 28-Jan-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRelatedInfoRequestDTO {
    private Long officeCitizenCharterId;
    private Long serviceId;
    private Long officeId;
    private String serviceName;
    private String officeName;
}
