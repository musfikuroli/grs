package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpiredGrievanceInfoDTO {
    Long id;
    String subject;
    String serviceName;
    Date closureDate;
    String groIdentifiedReason; // TODO: This field should be removed after replacing its usage by currentLocationList data in Reports module
    List<GrievanceCurrentLocationDTO> currentLocationList;
}
