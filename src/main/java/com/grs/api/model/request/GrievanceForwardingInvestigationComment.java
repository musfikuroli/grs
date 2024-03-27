package com.grs.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by User on 11/5/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceForwardingInvestigationComment {
    private Long grievanceId;
    private String decision;
    private List<FileDTO> signature;
}
