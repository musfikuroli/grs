package com.grs.api.model.response.grievance;

import com.grs.api.model.response.menu.SubMenuDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 18-Oct-17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrievanceMenuOptionContainerDTO {
    private List<GrievanceMenuOptionDTO> grievanceMenus;
}
