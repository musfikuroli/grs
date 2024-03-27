package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardDataDTO {
    GeneralDashboardDataDTO groDashboardData;
    GeneralDashboardDataDTO aoDashboardData;
    List<ChildOfficesDashboardNavigatorDTO> listOfChildOfficesData;
}
