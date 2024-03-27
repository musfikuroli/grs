package com.grs.api.model.response.organogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Acer on 10/4/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TreeNodeOfficerDTO {
    String id;
    String dataId;
    String text;
    String icon;
    String name;
    String designation;
    String officeUnitName;
    boolean children;
}
