package com.grs.core.domain.doptor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeInfo {
    private Integer id;
    private String name_eng;
    private String name_bng;
    private String father_name_eng;
    private String father_name_bng;
    private String mother_name_eng;
    private String mother_name_bng;
    private Date date_of_birth;
    private String nid;
    private String bcn;
    private String ppn;
    private String personal_email;
    private String personal_mobile;
    private Integer is_cadre;
    private String employee_grade;
    private Date joining_date;
    private Integer default_sign;
    private Integer gender;
    private String religion;
    private String blood_group;
    private String marital_status;
    private String alternative_mobile;
}
