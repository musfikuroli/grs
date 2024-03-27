package com.grs.api.model.response;

import com.grs.core.domain.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Acer on 9/6/2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOriginDTO {
    private Long id;
    private Long officeOriginId;
    private Long officeOriginUnitId;
    private Long officeOriginUnitOrganogramId;
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
    private List<EmployeeRecordDTO> responsible;
    private String officeOriginName;
    private String officeOriginUnitName;
    private String officeOriginUnitOrganogramName;
    private Boolean status;
    private Long countGrievanceByOffice;
}
