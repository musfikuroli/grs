package com.grs.api.model.response;

import com.grs.api.model.request.MeetingDTO;
import com.grs.api.model.response.file.FileDerivedDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 21-Mar-18.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingDetailsDTO {
    private MeetingDTO meeting;
    private List<GrievanceDTO> grievances;
    private String note;
    private List<FileDerivedDTO> files;
}
