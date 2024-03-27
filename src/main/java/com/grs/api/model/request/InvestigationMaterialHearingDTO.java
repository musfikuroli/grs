package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by User on 1/3/2018.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestigationMaterialHearingDTO {
    private Long grievanceId;
    private String note;
    private List<String> persons;
    private Date hearingDate;
}
