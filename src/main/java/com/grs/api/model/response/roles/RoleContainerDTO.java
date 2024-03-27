package com.grs.api.model.response.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 25-Dec-17.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleContainerDTO {
    List<SingleRoleDTO> roles;
}
