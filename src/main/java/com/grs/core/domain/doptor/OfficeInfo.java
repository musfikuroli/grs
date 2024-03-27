package com.grs.core.domain.doptor;

import com.grs.api.model.OfficeInformation;
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
public class OfficeInfo {
    private Integer id;
    private Integer employee_record_id;
    private Integer office_id;
    private Integer office_unit_id;
    private Integer office_unit_organogram_id;
    private String designation;
    private Integer designation_level;
    private Integer designation_sequence;
    private Integer office_head;
    private String incharge_label;
    private Date joining_date;
    private Date last_office_date;
    private boolean status;
    private Integer show_unit;
    private String designation_en;
    private String unit_name_bn;
    private String office_name_bn;
    private String unit_name_en;
    private String office_name_en;
    private Integer protikolpo_status;


    public OfficeInformation get() {
        return OfficeInformation.builder()
                .officeId(Long.parseLong(this.office_id + ""))
                .officeNameBangla(this.office_name_bn)
                .officeNameEnglish(this.office_name_en)
//                .officeMinistryId(oi.)
                .build();

    }
}
