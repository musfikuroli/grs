package com.grs.core.repo.grs;

import com.grs.core.domain.*;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.DashboardData;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class DashboardDataRepoImpl implements DashboardDataRepoCustom {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<GrievanceCountByService> getListOfGrievanceCountByService(Long officeId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GrievanceCountByService> criteriaQuery = criteriaBuilder.createQuery(GrievanceCountByService.class);
        Root<DashboardData> dashboardDataRoot = criteriaQuery.from(DashboardData.class);
        criteriaQuery.multiselect(
                criteriaBuilder.countDistinct(dashboardDataRoot.get("grievance").<Long>get("id")),
                dashboardDataRoot.<CitizenCharter>get("citizenCharter")
        );
        criteriaQuery.where(criteriaBuilder.equal(dashboardDataRoot.<Long>get("officeId"), officeId));
        criteriaQuery.groupBy(dashboardDataRoot.<CitizenCharter>get("citizenCharter"));
        Query query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }

    @Override
    public List<GrievanceCountByOfficeUnit> getListOfGrievanceCountByOfficeUnit(Long officeId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GrievanceCountByOfficeUnit> criteriaQuery = criteriaBuilder.createQuery(GrievanceCountByOfficeUnit.class);
        Root<DashboardData> dashboardDataRoot = criteriaQuery.from(DashboardData.class);
        criteriaQuery.multiselect(
                criteriaBuilder.countDistinct(dashboardDataRoot.get("grievance").<Long>get("id")),
                dashboardDataRoot.<Long>get("officeUnitId")
        );
        criteriaQuery.where(criteriaBuilder.equal(dashboardDataRoot.<Long>get("officeId"), officeId));
        criteriaQuery.groupBy(dashboardDataRoot.<Long>get("officeUnitId"));
        Query query = entityManager.createQuery(criteriaQuery);
        return query.getResultList();
    }


}
