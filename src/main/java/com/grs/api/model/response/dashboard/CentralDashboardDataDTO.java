package com.grs.api.model.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CentralDashboardDataDTO {
    private Long total;
    private Long resolved;
    private Long ascertain;
    private Float rate;
    private Long timeExpiredComplaints;
    private Long totalAppeal;
    private Long resolvedAppeal;
    private Long timeExpiredAppeal;
    private Long appealResolveRate;
    private List<ItemIdNameCountDTO> grievanceListDTO;
}
