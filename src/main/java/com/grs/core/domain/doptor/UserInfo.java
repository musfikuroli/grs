package com.grs.core.domain.doptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfo {
    private User user;
    private EmployeeInfo employee_info;
    private List<OfficeInfo> office_info;
    private HashMap<String, OfficeOrganogram> organogram_info;
}
