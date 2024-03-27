package com.grs.api.model.response.dashboard;

import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemIdNameCountDTO {
    private Long id;
    private String name;
    private Long grievanceCount;
}
