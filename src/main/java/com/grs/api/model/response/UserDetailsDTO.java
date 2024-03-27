package com.grs.api.model.response;

import com.grs.api.model.response.grievance.GrievanceShortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsDTO {
    private String username;
    private String email;
    private String phone;
    private List<GrievanceShortDTO> grievances;
}
