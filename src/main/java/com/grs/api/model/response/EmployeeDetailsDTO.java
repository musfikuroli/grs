package com.grs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 9/7/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDetailsDTO {
    private String id;
    private String name;
    private String designation;
    private String officeUnitNameBng;
    private String officeNameBng;
}
