package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by HP on 1/30/2018.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OccupationDTO {
    private Long Id;
    private String occupationBangla;
    private String occupationEnglish;
    private Boolean status;
}
