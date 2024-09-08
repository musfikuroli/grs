package com.grs.core.repo.grs;

import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.grs.DashboardData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DashboardDataRepo extends JpaRepository<DashboardData, Long> {

    @Query(value = "select coalesce(sum(cnt),0) from (select count(distinct complain_id) AS cnt from complain_history where current_status in ('NEW', 'RETAKE') " +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH)), '%Y-%m-%d 23:59:59')  " +
            "and medium_of_submission=?2 and office_id=?1) cxt", nativeQuery = true)


    Long countComplaintsByOfficeAndMediumOfSubmission(Long officeId, String mediumOfSubmission, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE '%CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id=?1 ")
    Long countResolvedGrievancesByOfficeId(Long officeId, Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT count(*) \n" +
                    "FROM dashboard_data d \n" +
                    "WHERE ((d.complaint_status LIKE '%APPEAL%'\n" +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' \n" +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' \n" +
                    " AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY) ,'%Y-%m-%d 00:00:00') \n" +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59')  \n" +
                    "OR d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY), '%Y-%m-%d 00:00:00') \n" +
                    "))\n" +
                    "or\n" +
                    "(d.complaint_status LIKE '%APPEAL%'\n" +
                    " AND (\n" +
                    " d.created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59') \n" +
                    " AND\n" +
                    "  (d.closed_date > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59') \n" +
                    " or\n" +
                    " (d.is_forwarded=true and d.updated_at > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59')  )\n" +
                    " )\n" +
                    "))) \n" +
                    "AND office_id=?1")
    Long getAppealsAscertainCountOfPreviousDay(Long officeId, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT count(*) " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.medium_of_submission=?2 " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 DAY))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1")
    Long countDailyAppealsByOfficeAndMediumOfSubmission(Long officeId, String mediumOfSubmission, Long dayDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE '%CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id in (:officeIds) ")
    Long countResolvedGrievancesByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE '%CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id in ?1 ")
    Long countResolvedGrievancesByOfficeId(List<Long> officeIds, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'FORWARDED%' " +
                    "AND (d.updated_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id=?1 ")
    Long countDeclinedGrievancesByOfficeId(Long officeId, Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'FORWARDED%' " +
                    "AND (d.updated_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id in (:officeIds) ")
    Long countDeclinedGrievancesByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'FORWARDED%' " +
                    "AND (d.updated_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id in ?1 ")
    Long countDeclinedGrievancesByOfficeId(List<Long> officeIds, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE '%CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id=?1 " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0" +
                    "")
    List<DashboardData> getResolvedGrievancesOfCurrentMonthByOfficeId(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1 " +
                    "")
    Long countTimeExpiredGrievancesByOfficeId(Long officeId, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL :monthDiff DAY " +
                    "AND office_id in (:officeIds) " +
                    "")
    Long countTimeExpiredGrievancesByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id in ?1 " +
                    "")
    Long countTimeExpiredGrievancesByOfficeId(List<Long> officeIds, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm    " +
                    "on cm.complaint_id = d.complaint_id    " +
                    "and cm.is_current = 1   " +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < ?3 - INTERVAL ?2 DAY " +
                    "AND office_id  in ?1  " +
                    "")
    Long countTimeExpiredGrievancesByOfficeId(List<Long> officeIds, Long dayDiff, Date fromDate);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1 " +
                    "")
    Long countAllTimeExpiredComplaintsByOfficeId(Long officeId, Long dayDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
//                    "AND d.closure_date IS NULL " +
                    "AND d.closed_date IS NULL " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?1 DAY " +
                    "")
    Long countTimeExpiredGrievances(Long dayDiff);
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.closed_date IS NULL " +
                    "AND d.created_at < :fromDate  " +
                    "")
    Long countTimeExpiredGrievances(@Param("fromDate") Date fromDate);

    //checked
    @Query(
            nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1 " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0 " +
                    "")
    List<DashboardData> getTimeExpiredGrievancesByOfficeId(Long officeId, Long dayDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59')) " +
                    "AND d.closed_date IS NULL " +
                    "AND d.is_forwarded=false " +
                    "AND office_id=?1 " +
                    "")
    Long countRunningGrievancesByOfficeId(Long officeId, Long monthDiff, Long dayDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), INTERVAL :dayDiff DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), '%Y-%m-%d 23:59:59')) " +
                    "AND d.closed_date IS NULL " +
                    "AND d.is_forwarded=false " +
                    "AND office_id in (:officeIds) " +
                    "")
    Long countRunningGrievancesByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff, @Param("dayDiff") Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59')) " +
                    "AND d.closed_date IS NULL " +
                    "AND d.is_forwarded=false " +
                    "AND office_id in ?1 " +
                    "")
    Long countRunningGrievancesByOfficeId(List<Long> officeIds, Long monthDiff, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE '%REJECTED%' " +
                    "AND d.complaint_status NOT LIKE 'CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59')) " +
                    "AND d.closed_date IS NULL " +
                    "AND d.is_forwarded=false " +
                    "AND office_id=?1 " +
                    "")
    Long countAllRunningGrievancesByOfficeId(Long officeId, Long monthDiff, Long dayDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE 'CLOSED%' " +
                    "       AND d.complaint_status NOT LIKE 'CLOSED\\_ACCUSATION\\_INCORRECT') " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1 " +
                    "")
    Long countResolvedGrievancesByOfficeIdAndIsReal(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'CLOSED\\_ACCUSATION\\_INCORRECT' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1 " +
                    "")
    Long countResolvedGrievancesByOfficeIdAndIsNotReal(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE  d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.office_id=?1 " +
                    "AND d.complaint_status=?2 " +
                    "")
    Long countComplaintsByOfficeIdAndStatus(Long officeId, String complaintStatus);

    //checked
    @Query(
            nativeQuery = true,
            value = "SELECT d.grievance_type, " +
                    "   (SELECT COUNT(DISTINCT d1.complaint_id) " +
                    "       FROM dashboard_data d1 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d1.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "       WHERE (d1.complaint_status NOT LIKE '%APPEAL%' " +
                    "       AND d1.grievance_type=d.grievance_type " +
                    "       AND ((d1.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "           AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "       OR (d1.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "           AND (d1.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d1.closed_date IS NULL AND d1.is_forwarded=false)))) " +
                    "       AND office_id=?1 " +
                    "))  AS TotalSubmitted, " +
                    "   (SELECT COUNT(DISTINCT d2.complaint_id) " +
                    "       FROM dashboard_data AS d2 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d2.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "       WHERE d2.complaint_status NOT LIKE '%APPEAL%' " +
                    "       AND d2.grievance_type=d.grievance_type " +
                    "       AND (d2.complaint_status LIKE 'CLOSED%' " +
                    "           OR d2.complaint_status LIKE '%REJECTED%') " +
                    "       AND (d2.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "           AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "       AND office_id=?1" +
                    ") AS TotalResolved " +
                    "FROM dashboard_data d " +
                    "WHERE d.office_id=?1 " +
                    "GROUP BY d.grievance_type")
    List getTotalAndResolvedGrievanceCountWithTypeByMonthAndYear(Long officeId, Integer monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE  d.complaint_status NOT LIKE '%APPEAL%' " +
                    " AND d.complaint_status NOT LIKE '%REJECTED%'\n" +
                    " AND d.complaint_status NOT LIKE 'CLOSED%' \n" +
//                    " AND d.complaint_status NOT LIKE '%FORWARDED_TO_AO%'" +
                    " AND d.complaint_status NOT LIKE '%FORWARDED%'" +
                    "AND (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00')) " +
                    "AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR " +
                    "(d.closed_date IS NULL AND d.is_forwarded=false)) " +
                    "AND d.office_id=?1 " +
                    "")
    Long getGrievanceAscertainCountOfPreviousMonth(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE  d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.case_number IS NOT NULL " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1 " +
                    "")
    Long countAcceptedGrievancesByOfficeIdAndMonthDiff(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.office_unit_id AS OfficeUnitId, " +
                    "       COUNT(DISTINCT d.complaint_id) AS TotalSubmitted " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "    AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')  " +
                    "  OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
//                    "    AND (d.closed_date > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') OR (d.closed_date IS NULL AND d.is_forwarded=false))))                      \n" +
                    "    AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.office_id=?1 " +
                    "GROUP BY d.office_unit_id")
    List getListOfGrievanceCountByOfficeUnitId(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.office_service_id AS CitizensCharterId, " +
                    "       COUNT(DISTINCT d.complaint_id) AS TotalSubmitted " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "    AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "  OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "    AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.office_id=?1 " +
                    "GROUP BY d.office_service_id")
    List getListOfGrievanceCountByServiceId(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.rating) AS average, COUNT(d.rating) AS total " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "  AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND office_id=?1 " +
                    ")")
    Object countAvgRatingOfComplaintsByOfficeId(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.complaint_id " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.rating IS NOT NULL " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(CURDATE() ,'%Y-%m-01 00:00:00') " +
                    " AND DATE_FORMAT(LAST_DAY(CURDATE()), '%Y-%m-%d 23:59:59')) " +
                    "OR (d.created_at < DATE_FORMAT(CURDATE(), '%Y-%m-01 00:00:00') " +
                    " AND (d.closed_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.office_id=?1 " +
                    ")")
    List getIdsOfGrievancesContainRatingInCurrentMonth(Long officeId);


    //endregion

    //region APPEAL


    //checked
    @Query(nativeQuery = true,
            value = "SELECT count(*) " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.appeal_from_office_id=?1")
    Long countGrievancesAppealedFromThisOffice(Long officeId, Long monthDiff);


    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "  AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND office_id=?1)")
    Long countTotalAppealsByOfficeId(Long officeId, Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH), '%Y-%m-01 00:00:00') " +
                    "  AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND office_id in (:officeIds)   )")
    Long countTotalAppealsByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE (d.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "OR d.complaint_status LIKE 'APPEAL\\_REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id=?1")
    Long countResolvedAppealsByOfficeId(Long officeId, Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE (d.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "OR d.complaint_status LIKE 'APPEAL\\_REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff  MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id in (:officeIds)  ")
    Long countResolvedAppealsByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE (d.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "OR d.complaint_status LIKE 'APPEAL\\_REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH))), '%Y-%m-%d 23:59:59'))  ")
    Long countResolvedAppeals(Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE (d.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "OR d.complaint_status LIKE 'APPEAL\\_REJECTED%') " +
                    "AND (d.closed_date BETWEEN :fromDate    AND :toDate )  ")
    Long countResolvedAppeals(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE 'APPEAL\\_REJECTED%' " +
                    "AND office_id=?1")
    Long countDeclinedAppealsByOfficeId(Long officeId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1")
    Long countTimeExpiredAppealsByOfficeId(Long officeId, Long dayDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?1 DAY ")
    Long countTimeExpiredAppeals(Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < :fromDate ")
    Long countTimeExpiredAppeals(@Param("fromDate") Date fromDate );

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59')) " +
                    "AND (d.closed_date > DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59') " +
                    "   OR (d.closed_date IS null AND d.is_forwarded=false)) " +
                    "AND office_id=?1")
    Long countRunningAppealsByOfficeId(Long officeId, Long monthDiff, Long dayDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT count(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.medium_of_submission=?2 " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1")
    Long countAppealsByOfficeAndMediumOfSubmission(Long officeId, String mediumOfSubmission, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data AS d " +
                    "WHERE (d.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "OR d.complaint_status LIKE 'APPEAL\\_REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND office_id=?1")
    List<DashboardData> getResolvedAppealsOfCurrentMonthByOfficeId(Long officeId, Long monthDiff);

    //checked
    @Query(
            nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1")
    List<DashboardData> getTimeExpiredAppealsByOfficeId(Long officeId, Long dayDiff);

    //checked
    @Query(
            nativeQuery = true,
            value = "SELECT d.appeal_from_office_id, COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "       AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "   OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "       AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND office_id=?1) " +
                    "GROUP BY d.appeal_from_office_id")
    List getCountOfAppealsBySourceOffices(Long officeId, Long monthDiff);

    //checked
    @Query(
            nativeQuery = true,
            value = "SELECT d.grievance_type, " +
                    "   (SELECT COUNT(DISTINCT d1.complaint_id) " +
                    "       FROM dashboard_data d1 " +
                    "       WHERE (d1.complaint_status LIKE '%APPEAL%' " +
                    "       AND d1.grievance_type=d.grievance_type " +
                    "       AND ((d1.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "           AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "       OR (d1.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "           AND (d1.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR d1.closed_date IS NULL))) " +
                    "       AND office_id=?1))  AS TotalSubmitted, " +
                    "   (SELECT COUNT(DISTINCT d2.complaint_id) " +
                    "       FROM dashboard_data AS d2 " +
                    "       WHERE d2.complaint_status LIKE 'APPEAL\\_CLOSED%' " +
                    "       AND d2.grievance_type=d.grievance_type " +
                    "       AND (d2.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "           AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "       AND office_id=?1) AS TotalResolved " +
                    "FROM dashboard_data d " +
                    "WHERE d.office_id=?1 " +
                    "GROUP BY d.grievance_type")
    List getTotalAndResolvedAppealCountWithTypeByMonthAndYear(Long officeId, Integer monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE  d.complaint_status LIKE '%APPEAL%' " +
                    "AND (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00')) " +
                    "AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)) " +
                    "AND d.office_id=?1")
    Long getAppealsAscertainCountOfPreviousMonth(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT AVG(d.rating) AS average, COUNT(d.rating) AS total " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "  AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND office_id=?1)")
    Object countAvgRatingOfAppealsByOfficeId(Long officeId, Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.office_id=?1 " +
                    "AND d.complaint_id=?2 " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0 " +
                    ")" +
                    "order by d.created_at asc")
    List<DashboardData> findNormalGrievanceByOfficeIdAndGrievanceId(Long officeId, Long grievanceId);

    DashboardData findTopByOfficeIdAndGrievanceId(Long officeId, Long grievanceId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.office_id=?1 " +
                    "AND d.complaint_id=?2 " +
                    ") " +
                    "order by created_at desc " +
                    "limit 1")
    DashboardData findAppealByOfficeIdAndGrievanceId(Long officeId, Long grievanceId);

    //endregion

    // region central

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH)), '%Y-%m-%d 23:59:59'))) " +
                    "")
    Long countTotalGrievances(Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE  d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00')) " +
                    "AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') OR " +
//                    "(d.closed_date IS NULL AND d.is_forwarded=false AND d.closure_date IS NULL))")
                    "(d.closed_date IS NULL)) " +
                    "")
    Long countGrievanceAscertainFromLastMonth(Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm    " +
                    "on cm.complaint_id = d.complaint_id    " +
                    "and cm.is_current = 1   " +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE  d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'FORWARDED%' " +
                    "AND (d.created_at   BETWEEN  :fromDate   AND  :toDate  )" +
                    "AND (d.closed_date >=  :fromDate  OR " +
                    "(d.closed_date IS NULL)) " +
                    "")
    Long countGrievanceAscertainFromLastMonth(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE 'CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH))), '%Y-%m-%d 23:59:59')) " +
                    "")
    Long countResolvedGrievances(Long monthDiff);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.service_id, s.service_name_bng, COUNT(d.service_id) as total " +
                    "FROM (dashboard_data d " +
                    "INNER JOIN services s " +
                    "ON d.service_id = s.id) " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "   AND d.service_id IS NOT null) " +
                    "AND (d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00'))" +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0 " +
                    "GROUP BY d.service_id " +
                    "ORDER BY total desc " +
                    "limit ?1")
    Object[] getCitizenCharterServicesByComplaintFrequency(Integer limit);

    @Query(nativeQuery = true,
            value = "SELECT d.service_id, s.service_name_bng, COUNT(d.service_id) as total " +
                    "FROM (dashboard_data d " +
                    "INNER JOIN services s " +
                    "ON d.service_id = s.id) " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "   AND d.service_id IS NOT null) " +
                    "AND (d.created_at BETWEEN :fromDate AND :toDate )" +
                    "  and " +
                    "(select count(*) from complaint_movements cm " +
                    "where cm.complaint_id = d.complaint_id  " +
                    "and cm.is_current = 1 " +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0 " +
                    "GROUP BY d.service_id " +
                    "ORDER BY total desc " +
                    "limit :limit")
    Object[] getCitizenCharterServicesByComplaintFrequency(@Param("fromDate") Date fromDate,@Param("toDate") Date toDate, @Param("limit") Integer limit);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.complaint_id " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.rating IS NOT NULL " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(CURDATE() ,'%Y-%m-01 00:00:00') " +
                    " AND DATE_FORMAT(LAST_DAY(CURDATE()), '%Y-%m-%d 23:59:59')) " +
                    "OR (d.created_at < DATE_FORMAT(CURDATE(), '%Y-%m-01 00:00:00') " +
                    " AND (d.closed_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.office_id=?1)")
    List<Long> getIdsOfAppealsContainRatingInCurrentMonth(Long officeId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT d.office_id, COUNT(d.service_id) as total " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "  AND d.service_id IS NOT null) " +
                    "AND d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00') " +
                    "AND d.service_id = ?1 " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0 " +
                    "GROUP BY d.office_id " +
                    "ORDER BY total desc ")
    Object[] getServiceCountWithOfficeNameByServiceId(Long serviceId);

    @Query(nativeQuery = true,
            value = "SELECT d1.office_id AS office_id, COUNT(DISTINCT d1.complaint_id) AS TotalSubmitted " +
                    "      FROM dashboard_data d1 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d1.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "            WHERE (d1.complaint_status NOT LIKE '%APPEAL%' \n" +
                    "      AND d1.complaint_status NOT LIKE '%REJECTED%'\n" +
                    "      AND d1.complaint_status NOT LIKE 'CLOSED%' \n" +
                    "      AND d1.complaint_status NOT LIKE '%FORWARDED%'  " +
                    "      AND (d1.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "          AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "      OR (d1.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') " +
                    "          AND (d1.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') OR (d1.closed_date IS NULL AND d1.is_forwarded=false)))) " +
                    "      AND d1.office_id IN ?2 " +
                    "       GROUP BY d1.office_id;")
    Object[] getSubmittedCountByOfficeIdInList(Long monthDiff, List<Long> officeIds);

    @Query(nativeQuery = true,
            value = "SELECT d2.office_id AS office_id, COUNT(DISTINCT d2.complaint_id) AS TotalResolved " +
                    "      FROM dashboard_data AS d2 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d2.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "      WHERE d2.complaint_status NOT LIKE '%APPEAL%' " +
                    "      AND (d2.complaint_status LIKE 'CLOSED%' " +
                    "           OR d2.complaint_status LIKE '%REJECTED%') " +
                    "      AND (d2.closed_date BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "          AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "      AND d2.office_id IN ?2 " +
                    "      GROUP BY d2.office_id;")
    Object[] getResolvedCountByOfficeIdInList(Long monthDiff, List<Long> officeIds);


    @Query(nativeQuery = true,
            value = "SELECT d3.office_id AS office_id, COUNT(DISTINCT d3.complaint_id) AS TotalExpired " +
                    "       FROM dashboard_data AS d3 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d3.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "       WHERE d3.complaint_status NOT LIKE '%APPEAL%' " +
                    "           AND d3.complaint_status NOT LIKE 'CLOSED%' " +
                    "           AND d3.complaint_status NOT LIKE '%REJECTED%' " +
                    "           AND d3.complaint_status NOT LIKE 'FORWARDED%' " +
                    "           AND d3.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "           AND d3.office_id IN ?1 " +
                    "           GROUP BY d3.office_id;")
    Object[] getExpiredCountByOfficeIdInList(List<Long> officeIds, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT d3.office_id AS office_id, COUNT(DISTINCT d3.complaint_id) AS TotalExpired " +
                    "       FROM dashboard_data AS d3 " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d3.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "       WHERE d3.complaint_status NOT LIKE '%APPEAL%' " +
                    "           AND d3.complaint_status NOT LIKE 'CLOSED%' " +
                    "           AND d3.complaint_status NOT LIKE '%REJECTED%' " +
                    "           AND d3.complaint_status NOT LIKE 'FORWARDED%' " +
                    "           AND d3.created_at < ?3 - INTERVAL ?2 DAY " +
                    "           AND d3.office_id IN ?1 " +
                    "           GROUP BY d3.office_id;")
    Object[] getExpiredCountByOfficeIdInList(List<Long> officeIds, Long dayDiff, Date fromDate);


    // endregion

    // region adhoc

    //checked
    @Query(nativeQuery = true,
            value = "SELECT *  " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status NOT LIKE '%APPEAL%' " +
                    "   AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "       AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "   OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') " +
                    "       AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.case_number IS NOT NULL " +
                    "AND office_id=?2 " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0" +
                    ") " +
                    "ORDER BY d.created_at desc")
    List<DashboardData> getDashboardDataForCurrentMonthGrievanceRegister(Long monthDiff, Long officeId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data d " +
                    "WHERE (d.complaint_status LIKE '%APPEAL%' " +
                    "   AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "       AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "   OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') " +
                    "       AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false)))) " +
                    "AND d.case_number IS NOT NULL " +
                    "AND office_id=?2)" +
                    "ORDER BY d.created_at desc")
    List<DashboardData> getDashboardDataForCurrentMonthAppealRegister(Long monthDiff, Long officeId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT * " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_id=?1 " +
                    "AND d.complaint_status NOT LIKE '%APPEAL%' " +
                    "  and\n" +
                    "(select count(*) from complaint_movements cm\n" +
                    "where cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%') > 0")
    DashboardData findByGrievanceIdAndComplaintStatusNotAppeal(Long grievanceId);

    Page<DashboardData> findByOfficeIdAndComplaintStatusInOrderByCreatedAtDesc(Long officeId, List<GrievanceCurrentStatus> grievanceCurrentStatusList, Pageable pageable);

    // New method for filtering by officeId, tracking number, and non-appeal statuses
    Page<DashboardData> findByOfficeIdAndTrackingNumberAndComplaintStatusInOrderByCreatedAtDesc(Long officeId, String trackingNumber, List<GrievanceCurrentStatus> statusList, Pageable pageable);

    Page<DashboardData> findByOfficeIdAndGrievanceIdInAndAppealFromOfficeIdIsNullOrderByCreatedAtDesc(Long officeId, List<Long> grievanceIdList, Pageable pageable);

    List<DashboardData> findByAppealFromOfficeId(Long officeId);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH)), '%Y-%m-%d 23:59:59'))  " +
                    "OR (d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') " +
                    "  AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?1 MONTH), '%Y-%m-01 00:00:00') OR (d.closed_date IS NULL AND d.is_forwarded=false))))")
    Long countTotalAppeals(Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND ((d.created_at BETWEEN :fromDate    AND :toDate )  " +
                    "OR (d.created_at < :fromDate " +
                    "  AND (d.closed_date >= :fromDate  OR (d.closed_date IS NULL AND d.is_forwarded=false))))")
    Long countTotalAppeals(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00')) " +
                    "")
    Long countTotalGrievancesYearly();

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.created_at BETWEEN :fromDate AND   :toDate  " +
                    "")
    Long countTotalGrievances(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'FORWARDED%' " +
                    "AND (d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00'))" +
                    "")
    Long countForwardedGrievancesYearly();

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND d.complaint_status LIKE 'FORWARDED%' " +
                    "AND (d.created_at BETWEEN :fromDate AND   :toDate )" +
                    "")
    Long countForwardedGrievances(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE 'CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00'))" +
                    "")
    Long countResolvedGrievancesYearly();

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) " +
                    "FROM dashboard_data AS d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.complaint_status LIKE 'CLOSED%' " +
                    "   OR d.complaint_status LIKE '%REJECTED%') " +
                    "AND (d.closed_date  BETWEEN :fromDate AND   :toDate )" +
                    "")
    Long countResolvedGrievances(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data d where d.complaint_status NOT LIKE '%APPEAL%' AND " +
                    "\t(created_at BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND office_id = ?1")
    Long countTotalComplaintsByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%' AND " +
                    "\t( dd.closed_date BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\t\t\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')\n" +
                    "\t) AND dd.office_id = ?1")
    Long countResolvedComplaintsByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%' AND\n" +
                    "\t(dd.created_at < DATE_FORMAT(DATE_ADD(?2, INTERVAL -30 DAY), '%Y-%m-%d 00:00:00')) AND\n" +
                    "\t((dd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00') OR (dd.closed_date IS NULL AND dd.is_forwarded=false))\n" +
                    "\t\tAND (dd.is_forwarded = 0 OR (dd.is_forwarded = 1 AND dd.updated_at > DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')))\n" +
                    "\t) AND dd.office_id = ?1")
    Long countTimeExpiredComplaintsByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%' AND\n" +
                    "\t( dd.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(?2, INTERVAL 0 MONTH), INTERVAL -30 DAY), '%Y-%m-%d 00:00:00') AND \n" +
                    "\t\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00') \n" +
                    "\t) AND ( (dd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00') OR  (dd.closed_date IS NULL AND dd.is_forwarded=false))\n" +
                    "\t\tAND (dd.is_forwarded = 0 OR (dd.is_forwarded = 1 AND dd.updated_at > DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')))\n" +
                    "\t)  AND dd.office_id = ?1")
    Long countRunningGrievancesByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%' AND " +
                    "\tdd.complaint_status like \"%FORWARDED%\" AND dd.is_forwarded = TRUE AND \n" +
                    "\tdd.updated_at BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00') AND dd.office_id = ?1")
    Long countDeclinedGrievancesByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%'" +
                    "\tAND (dd.created_at < DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00'))\n" +
                    "\tAND ((dd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') OR (dd.closed_date IS NULL AND dd.closure_date IS NULL))" +
                    "\tAND (dd.is_forwarded = 0 OR (dd.is_forwarded = 1 AND dd.updated_at > DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00')))) " +
                    "AND dd.office_id = ?1")
    Long getGrievanceAscertainCountbyOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status NOT LIKE '%APPEAL%' AND" +
                    "\t(created_at BETWEEN DATE_FORMAT(DATE_ADD(?3, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?3, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND dd.office_id=?1 AND dd.medium_of_submission=?2")
    Long getMonthlyComplaintsCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, String mediumOfSubmission, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status LIKE '%APPEAL%' AND" +
                    "\t(dd.created_at BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND\n" +
                    "\t( dd.closed_date BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\t\t\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')\n" +
                    "\t) AND dd.office_id = ?1")
    Long countResolvedAppealByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status LIKE '%APPEAL%' AND" +
                    "\t(dd.created_at < DATE_FORMAT(DATE_ADD(?2, INTERVAL 30 DAY), '%Y-%m-01 00:00:00')) AND\n" +
                    "\t dd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00') \n" +
                    "\t AND dd.office_id = ?1")
    Long countTimeExpiredAppealByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status LIKE '%APPEAL%' AND" +
                    "\t(dd.created_at BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND\n" +
                    "\tdd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')\n" +
                    "\tAND dd.office_id = ?1")
    Long countRunningAppealByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data d where d.complaint_status LIKE '%APPEAL%' AND " +
                    "\t(created_at BETWEEN DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND office_id = ?1")
    Long countTotalAppealByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status LIKE '%APPEAL%' AND" +
                    "\t(created_at < DATE_FORMAT(DATE_ADD(?2, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00')) \n" +
                    "\tand dd.closed_date >= DATE_FORMAT(DATE_ADD(?2, INTERVAL 1 MONTH), '%Y-%m-01 00:00:00') \n" +
                    "\tAND dd.office_id = ?1")
    Long getAppealAscertainCountByOfficeIdAndYearAndMonth(Long officeId, String date);

    //checked -- regenerate -- unnecessary
    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT dd.complaint_id) FROM dashboard_data dd where dd.complaint_status LIKE '%APPEAL%' AND" +
                    "\t(created_at BETWEEN DATE_FORMAT(DATE_ADD(?3, INTERVAL 0 MONTH), '%Y-%m-01 00:00:00') AND \n" +
                    "\tDATE_FORMAT(DATE_ADD(?3, INTERVAL 1 MONTH), '%Y-%m-%d 00:00:00')) AND dd.office_id=?1 AND dd.medium_of_submission=?2")
    Long getMonthlyAppealCountByOfficeIdAndMediumOfSubmissionAndYearAndMonth(Long officeId, String mediumOfSubmission, String date);

    @Query(value = "select coalesce(sum(cnt),0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('NEW','RETAKE')" +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') and office_id=?1) cxt", nativeQuery = true)


    Long countTotalComplaintsByOfficeIdV2(Long officeId, Long monthDiff);


    @Query(value =
            "SELECT COALESCE(SUM(cnt), 0) " +
                "FROM ("+
                "SELECT COUNT(DISTINCT complain_id) AS cnt "+
                "FROM complain_history "+
                "WHERE current_status IN ('NEW', 'RETAKE') "+
                "AND (closed_at IS NULL OR closed_at > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH)), '%Y-%m-%d 23:59:59')) "+
                "AND (created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH)), '%Y-%m-%d 23:59:59')) "+
                "AND office_id = ?1 "+
                "AND complain_id NOT IN ( "+
                "SELECT DISTINCT complain_id "+
                "FROM complain_history "+
                "WHERE current_status IN ('NEW','RETAKE') "+
                "AND created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') "+
                "AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') "+
                "AND office_id = ?1) "+
            ") cxt ", nativeQuery = true)
    Long countInheritedComplaintsByOfficeId(Long officeId, Long currMonth, Long prevMonth);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d \n" +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE ((d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND d.complaint_status NOT LIKE '%REJECTED%'\n" +
                    " AND d.complaint_status NOT LIKE 'CLOSED%' \n" +
                    " AND d.complaint_status NOT LIKE '%FORWARDED%'\n" +
                    " AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH) ,'%Y-%m-01 00:00:00') \n" +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH)), '%Y-%m-%d 23:59:59')  \n" +
                    "OR d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), '%Y-%m-01 00:00:00') \n" +
                    "))\n" +
                    "or\n" +
                    "(d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND (\n" +
                    " d.created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH)), '%Y-%m-%d 23:59:59')\n" +
                    " AND\n" +
                    " (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH) ,'%Y-%m-01 00:00:00') \n" +
                    " or\n" +
                    " (d.is_forwarded=true and d.updated_at >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH) ,'%Y-%m-01 00:00:00') )\n" +
                    " )\n" +
                    ")))" +
                    "AND office_id in (:officeIds)" +
                    "")
    Long countTotalComplaintsByOfficeIdsV2(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d \n" +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE ((d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND d.complaint_status NOT LIKE '%REJECTED%'\n" +
                    " AND d.complaint_status NOT LIKE 'CLOSED%' \n" +
                    " AND d.complaint_status NOT LIKE '%FORWARDED%'\n" +
                    " AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY) ,'%Y-%m-%d 00:00:00') \n" +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59')  \n" +
                    "OR d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY), '%Y-%m-%d 00:00:00') \n" +
                    "))\n" +
                    "or\n" +
                    "(d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND (\n" +
                    " d.created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 DAY)), '%Y-%m-%d 23:59:59')\n" +
                    " AND\n" +
                    " (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY) ,'%Y-%m-%d 00:00:00') \n" +
                    " or\n" +
                    " (d.is_forwarded=true and d.updated_at >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 DAY) ,'%Y-%m-%d 00:00:00') )\n" +
                    " )\n" +
                    ")))" +
                    "AND office_id=?1" +
                    "")
    Long countTotalComplaintsByOfficeIdV3(Long officeId, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d \n" +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE ((d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND d.complaint_status NOT LIKE '%REJECTED%'\n" +
                    " AND d.complaint_status NOT LIKE 'CLOSED%' \n" +
                    " AND d.complaint_status NOT LIKE '%FORWARDED%'\n" +
                    " AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') \n" +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')  \n" +
                    "OR d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') \n" +
                    "))\n" +
                    "or\n" +
                    "(d.complaint_status NOT LIKE '%APPEAL%'\n" +
                    " AND (\n" +
                    " d.created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')\n" +
                    " AND\n" +
                    " (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') \n" +
                    " or\n" +
                    " (d.is_forwarded=true and d.updated_at >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') )\n" +
                    " )\n" +
                    ")))" +
                    "AND office_id in ?1" +
                    "")
    Long countTotalComplaintsByOfficeIdV2(List<Long> officeIds, Long monthDiff);

    /*
    @Query(value = "select count( distinct complaint_id)\n" +
            "from dashboard_data d, complaints com\n" +
            "where d.complaint_id=com.id and d.complaint_status NOT LIKE '%APPEAL%' and com.current_status NOT LIKE '%APPEAL%' \n" +
            "  AND (\n" +
            "        (\n" +
            "                    d.complaint_status NOT LIKE 'CLOSED_%' and com.current_status NOT LIKE 'CLOSED_%' \n" +
            "                AND d.complaint_status NOT LIKE '%REJECTED%' AND com.current_status NOT LIKE '%REJECTED%'\n" +
            "                AND d.complaint_status NOT LIKE '%FORWARDED%' AND com.current_status NOT LIKE '%FORWARDED%'\n" +
            "                AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), d.created_at) > 0\n" +
            "                AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), d.created_at) > ?3\n" +
            "            )\n" +
            "        OR\n" +
            "        ( \n" +
            "                (\n" +
            "                            d.complaint_status LIKE 'CLOSED%'\n" +
            "                        OR d.complaint_status LIKE '%REJECTED%'\n" +
            "                        OR d.complaint_status LIKE '%FORWARDED%'\n" +
            "                    )\n" +
            "                AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), d.created_at) > 0\n" +
            "                AND DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), d.created_at) > ?3\n" +
            "                AND d.closed_date IS NOT NULL\n" +
            "                AND d.closed_date >= DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')\n" +
            "            )\n" +
            "    )\n" +
            "\n" +
            "  AND d.office_id = ?1 and com.office_id= ?1", nativeQuery = true)
        */

    @Query(value = "select coalesce(sum(cnt), 0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('NEW','RETAKE') " +
            "and created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and closed_at is null and DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), created_at) > ?3 " +
            "and office_id =?1) cxt", nativeQuery = true)

    Long countTimeExpiredGrievancesByOfficeIdV2(Long officeId, Long monthDiff, Long numberOfDays);

    @Query(value = "select coalesce(sum(cnt), 0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('NEW', 'RETAKE') " +
            "and (closed_at is null or closed_at > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')) " +
            "and office_id = ?1 and created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')) cxt", nativeQuery = true)

    Long countRunningGrievancesByOfficeIdV2(Long officeId, Long monthDiff);

    @Query(value = "select count(distinct complain_id) from complain_history where current_status in ('NEW', 'FORWARDED_IN') " +
            "and created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00')\n" +
            "and (closed_at > DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') or closed_at is null) " +
            "and office_id =?1", nativeQuery = true)


    Long getGrievanceAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff);

    @Query(value = "select count(distinct complain_id) from complain_history where current_status ='CLOSED' " +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') " +
            "and office_id=?1", nativeQuery = true)
    Long countResolvedGrievancesByOfficeIdV2(Long officeId, Long monthDiff);

    @Query(value = "select count(distinct complain_id) from complain_history where current_status='FORWARDED_OUT' " +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') " +
            "and office_id=?1", nativeQuery = true)


    Long countForwardedGrievancesByOfficeIdV2(Long officeId, Long monthDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(DISTINCT d.id) " +
                    "FROM dashboard_data d " +
                    "cross join complaint_movements cm \n" +
                    "on cm.complaint_id = d.complaint_id \n" +
                    "and cm.is_current = 1\n" +
                    "and cm.`action` not like '%APPEAL%' " +
                    "and cm.current_status not like '%APPEAL%' " +
                    "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') " +
                    "   AND DATE_FORMAT((LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH))), '%Y-%m-%d 23:59:59'))  " +
                    "AND d.office_id=?1" +
                    "")
    Long countComplaintsByOfficeOfAnyMediumOfSubmissionV2(Long officeId, Long monthDiff);


    // appeal region

    @Query(nativeQuery = true,
            value = "SELECT count(*) \n" +
                    "FROM dashboard_data d \n" +
                    "WHERE ((d.complaint_status LIKE '%APPEAL%'\n" +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' \n" +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' \n" +
                    " AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH) ,'%Y-%m-01 00:00:00') \n" +
                    "  AND DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')  \n" +
                    "OR d.created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') \n" +
                    "))\n" +
                    "or\n" +
                    "(d.complaint_status LIKE '%APPEAL%'\n" +
                    " AND (\n" +
                    " d.created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') \n" +
                    " AND\n" +
                    "  (d.closed_date > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') \n" +
                    " or\n" +
                    " (d.is_forwarded=true and d.updated_at > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')  )\n" +
                    " )\n" +
                    "))) \n" +
                    "AND office_id=?1")
    Long getAppealsAscertainCountOfPreviousMonthV2(Long officeId, Long monthDiff);


    @Query(nativeQuery = true,
            value = "select coalesce(sum(cnt),0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('APPEAL')" +
                    "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
                    "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') and office_id=?1) cxt")
    Long countTotalAppealsByOfficeIdV2(Long officeId, Long monthDiff);


    @Query(value = "select coalesce(sum(cnt), 0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('APPEAL') " +
            "and created_at < DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and closed_at is null and DATEDIFF(DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00'), created_at) > ?3 " +
            "and office_id =?1) cxt", nativeQuery = true)
    Long countTimeExpiredAppealsByOfficeIdV2(Long officeId, Long monthDiff, Long days);


    @Query(value = "select coalesce(sum(cnt), 0) from (select count(distinct complain_id) as cnt from complain_history where current_status in ('APPEAL') " +
            "and (closed_at is null or closed_at > DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')) " +
            "and office_id = ?1 and created_at < DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59')) cxt", nativeQuery = true)
    Long countRunningAppealsByOfficeIdV2(Long officeId, Long monthDiff);


    @Query(value = "select coalesce(sum(cnt),0) from (select count(distinct complain_id) AS cnt from complain_history where current_status in ('APPEAL') " +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?3 MONTH)), '%Y-%m-%d 23:59:59')  " +
            "and medium_of_submission=?2 and office_id=?1) cxt", nativeQuery = true)
    Long countAppealsByOfficeAndMediumOfSubmissionV2(Long officeId, String mediumOfSubmission, Long monthDiff);


    @Query(value = "select count(distinct complain_id) from complain_history where current_status ='APPEAL_CLOSED' " +
            "and created_at BETWEEN DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-01 00:00:00') " +
            "and DATE_FORMAT(LAST_DAY(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH)), '%Y-%m-%d 23:59:59') " +
            "and office_id=?1", nativeQuery = true)
    Long countResolvedAppealsByOfficeIdV2(Long officeId, Long monthDiff);


    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), INTERVAL ?3 DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59')) " +
//                    "AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59') " +
                    "AND (d.closed_date > DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59') " +
                    "   OR (d.closed_date IS null AND d.is_forwarded=false)) " +
                    "AND office_id=?1")
    Long countAllRunningAppealsByOfficeId(Long officeId, Long monthDiff, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND (d.created_at BETWEEN DATE_FORMAT(DATE_ADD(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), INTERVAL :dayDiff DAY) ,'%Y-%m-%d 00:00:00') " +
                    "   AND DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), '%Y-%m-%d 23:59:59')) " +
//                    "AND (d.closed_date >= DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL ?2 MONTH), '%Y-%m-%d 23:59:59') " +
                    "AND (d.closed_date > DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL :monthDiff MONTH), '%Y-%m-%d 23:59:59') " +
                    "   OR (d.closed_date IS null AND d.is_forwarded=false)) " +
                    "AND office_id in (:officeIds) ")
    Long countAllRunningAppealsByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("monthDiff") Long monthDiff, @Param("dayDiff") Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL ?2 DAY " +
                    "AND office_id=?1")
    Long countAllTimeExpiredAppealsByOfficeId(Long officeId, Long dayDiff);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM dashboard_data AS d " +
                    "WHERE d.complaint_status LIKE '%APPEAL%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_CLOSED%' " +
                    "AND d.complaint_status NOT LIKE 'APPEAL\\_REJECTED%' " +
                    "AND d.created_at < CURRENT_DATE - INTERVAL :dayDiff DAY " +
                    "AND office_id in (:officeIds) ")
    Long countAllTimeExpiredAppealsByOfficeIds(@Param("officeIds") List<Long> officeIds, @Param("dayDiff") Long dayDiff);

    // end region

}