package com.grs.core.repo.grs;

import com.grs.core.domain.GrievanceCountByOfficeUnit;
import com.grs.core.domain.GrievanceCountByService;

import java.util.List;

public interface DashboardDataRepoCustom {
    List<GrievanceCountByService> getListOfGrievanceCountByService(Long officeId);
    List<GrievanceCountByOfficeUnit> getListOfGrievanceCountByOfficeUnit(Long officeId);
}
