package com.grs.api.model.response.grievance;

import com.grs.api.model.response.ServiceOriginDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.core.domain.GrievanceCurrentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 9/17/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceDetailsDTO {
    private GrievanceDTO grievance;
    private String details;
    private ServiceOriginDTO service;
    private String officeNameBangla;
    private String officeNameEnglish;
    private String userType;
    private ComplainantInfoDTO complainant;
    private List<FileDerivedDTO> files;
    private GrievanceMenuOptionContainerDTO menuOptions;
    private String groPost;
    private String soPost;
}
