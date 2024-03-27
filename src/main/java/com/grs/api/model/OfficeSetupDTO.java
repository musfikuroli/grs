package com.grs.api.model;

import com.grs.api.model.response.CitizenCharterDTO;
import com.grs.api.model.response.OfficesGroDTO;
import com.grs.api.model.response.ServiceOriginDTO;
import com.grs.core.domain.grs.ServiceOrigin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 4/10/2018.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeSetupDTO {
    private OfficesGroDTO officesGroDTO;
    private ServiceOriginDTO serviceOriginDTO;
    private CitizenCharterDTO citizenCharterDTO;
}
