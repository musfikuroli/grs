package com.grs.core.repo.grs;

import com.grs.core.domain.grs.DashboardData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 22-Feb-18.
 */
@Repository
public interface ReportsRepo extends JpaRepository<DashboardData, Long> {
    @Query(nativeQuery = true,
            value = "SELECT count(*) " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status NOT LIKE 'APPEAL_%' and  d.complaint_status NOT LIKE '%_APPEAL'  " +
                    "AND d.office_id=?1 " +
                    "AND d.medium_of_submission=?2 AND d.submission_date >= ?3 and d.submission_date < date_add(?3, INTERVAL 1 MONTH))")
    Long countByOfficeAndMediumOfSubmissionAndDateInBetween(Long officeId, String mediumOfSubmission, Date date);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d1.complaint_id) " +
                    "FROM dashboard_data AS d1 " +
                    "WHERE d1.complaint_status NOT LIKE 'APPEAL_%' and d1.complaint_status NOT LIKE '%_APPEAL' " +
                    "AND (d1.complaint_status LIKE '%REJECTED%' " +
                    "OR d1.complaint_status LIKE 'CLOSED%' " +
                    "OR d1.complaint_status LIKE 'FORWARDED%') " +
                    "AND office_id=?1 AND d1.submission_date >= ?2 and d1.submission_date < date_add(?2, INTERVAL 1 MONTH) ")
    Long countResolvedGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d1.complaint_id) " +
                    "FROM dashboard_data AS d1 " +
                    "WHERE d1.complaint_status NOT LIKE 'APPEAL_%' and d1.complaint_status NOT LIKE '%_APPEAL' " +
                    "AND d1.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d1.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d1.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d1.created_at <= CURRENT_DATE - INTERVAL '" + 90 + "' DAY " +
                    "AND office_id=?1 AND d1.submission_date >= ?2 and d1.submission_date < date_add(?2, INTERVAL 1 MONTH) ")
    Long countUnresolvedGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d1.complaint_id) " +
                    "FROM dashboard_data AS d1 " +
                    "WHERE d1.complaint_status NOT LIKE 'APPEAL_%' and d1.complaint_status NOT LIKE '%_APPEAL' " +
                    "AND d1.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d1.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d1.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d1.created_at >= CURRENT_DATE - INTERVAL '" + 90 + "' DAY " +
                    "AND office_id=?1 AND d1.submission_date >= ?2 and d1.submission_date < date_add(?2, INTERVAL 1 MONTH) ")
    Long countRunningGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date);
}
