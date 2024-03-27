package com.grs.api.model.request;

import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by User on 11/26/2017.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceForwardingCloseDTO {
    private Boolean takeAction;
    private String departmentalActionNote;
    private List<String> employeeList;
    private String groDecision;
    private String mainReason;
    private String groSuggestion;
    private GrievanceCurrentStatus status;
    private List<FileDTO> files;
    private List<Long> referredFiles;
}
