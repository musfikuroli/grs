package com.grs.api.model.response;

import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by User on 10/19/2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CitizenCharterDTO {
    private Long id;
    private Long officeId;
    private Long officeOriginId;
    private Long soOfficeId;
    private Long soOfficeUnitId;
    private String soOfficeUnitName;
    private Long soOfficeUnitOrganogramId;
    private Long officeUnitOrganogramId;
    private Long serviceId;
    private String serviceNameBangla;
    private String serviceNameEnglish;
    private String serviceProcedureBangla;
    private String serviceProcedureEnglish;
    private String documentAndLocationBangla;
    private String documentAndLocationEnglish;
    private String paymentMethodBangla;
    private String paymentMethodEnglish;
    private Integer serviceTime;
    private ServiceType serviceType;
    private String name;
    private String mobileNumber;
    private String designation;
    private String officeUnitName;
    private String officeName;
    private Boolean status;
    private Boolean originStatus;
    private List<Long> soOfficeIds;
    private List<Long> soOfficeUnitIds;
    private List<Long> soOfficeUnitOrganogramIds;
    private List<String> names;
    private List<String> designations;
    private List<String> phoneNumbers;
    private List<String> officeNames;
    private List<String> officeUnits;
}
