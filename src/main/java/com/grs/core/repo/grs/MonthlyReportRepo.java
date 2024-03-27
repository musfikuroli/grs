package com.grs.core.repo.grs;

import com.grs.core.domain.grs.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyReportRepo extends JpaRepository<MonthlyReport, Long> {

    MonthlyReport findByOfficeIdAndYearAndMonth(Long officeId, Integer year, Integer month);

    List<MonthlyReport> findByOfficeIdAndYear(Long officeId, Integer year);

    List<MonthlyReport> findByOfficeIdInAndYear(List<Long> officeIds, Integer year);

    List<MonthlyReport> findByOfficeIdAndMonthAndYear(Long officeId, Integer month, Integer year);

    List<MonthlyReport> findByOfficeIdInAndMonthAndYear(List<Long> officeIds, Integer month, Integer year);


    @Query(value = "select      year  ,sum(total)  as totalCount  , sum(resolved) as resolvedCount  " +
            "from monthly_report " +
            "where office_id=:officeId " +
            "group by  year " +
            "order by year", nativeQuery = true)
    List<Object[]> findByOfficeIdGroupByYear(@Param("officeId") Long officeId);

    @Query(value = "select      year  ,sum(total)  as totalCount  , sum(resolved) as resolvedCount  " +
            "from monthly_report " +
            "where  " +
            " office_id=:officeId " +
            " and  year=:year " +
            "group by  year " +
            "order by year", nativeQuery = true)
    List<Object[]> findByOfficeIdAndYearGroupByYear(@Param("officeId") Long officeId, @Param("year") Integer year);


    @Query(value = "select      year,month, " +
            " sum(total)  as totalCount  , sum(resolved) as resolvedCount  , " +
            " sum(appeal_total)  as appealTotalCount  , sum(appeal_resolved) as appealResolvedCount   " +
            "from monthly_report " +
            "where office_id in (:officeIds)  and year =:year  and month =:month " +
            " group by year,month " +
            " order by year,month "
            , nativeQuery = true)
    List<Object[]> findByOfficeIdsAndYearAndMonthGroupByYearAndMonthOrderByYearAndMonth(@Param("officeIds") List<Long> officeIds, @Param("year") Integer year, @Param("month") Integer month);

    @Query(value = "select      year,month, " +
            " sum(total)  as totalCount  , sum(resolved) as resolvedCount , " +
            " sum(appeal_total)  as appealTotalCount  , sum(appeal_resolved) as appealResolvedCount   " +
            "from monthly_report " +
            "where office_id in (:officeIds)  and year =:year   " +
            "group by year,month " +
            "order by year,month "

            , nativeQuery = true)
    List<Object[]> findByOfficeIdsAndYearGroupByYearAndMonthOrderByYearAndMonth(@Param("officeIds") List<Long> officeIds, @Param("year") Integer year);

    Integer countByMonthAndYear(Integer month, Integer year);


    @Query(value = "select mr.year, " +
            "       mr.month, " +
            "       sum(mr.online_submission)                                 online_submission, " +
            "       sum(mr.conventional_method_submission)                    conventional_method_submission, " +
            "       sum(mr.self_motivated_accusation)                         self_motivated_accusation, " +
            "       sum(mr.inherited_from_last_month)                         inherited_from_last_month, " +
            "       sum(mr.total)                                             total, " +
            "       sum(mr.sent_to_other)                                     sent_to_other, " +
            "       sum(mr.resolved)                                          resolved, " +
            "       sum(mr.sent_to_other + mr.resolved)                       not_expired, " +
            "       sum(mr.time_expired)                                      time_expired, " +
            "       sum(mr.sent_to_other + mr.resolved) * 100 / sum(mr.total) rate, " +
            "       sum(mr.appeal_online_submission)                     appeal_online_submission, " +
            "       sum(mr.appeal_inherited_from_last_month)             appeal_inherited_from_last_month, " +
            "       sum(mr.appeal_total)                                 appeal_total, " +
            "       sum(mr.appeal_resolved)                              appeal_resolved, " +
            "       sum(mr.appeal_running + mr.appeal_resolved)          not_expired, " +
            "       sum(mr.appeal_time_expired)                          appeal_time_expired, " +
            "       sum(mr.appeal_resolved) * 100 / sum(mr.appeal_total) appeal_rate" +
            " from monthly_report mr " +
            " where mr.year >= :fromYear " +
            "  and mr.month >= :fromMonth " +
            "  and mr.year <= :toYear " +
            "  and mr.month <= :toMonth " +
            " group by mr.year, mr.month"

            , nativeQuery = true)
    List<Object[]> findSummaryByfromYearMonthToYearMonth(@Param("fromYear") int fromYear,
                                                         @Param("fromMonth") int fromMonth,
                                                         @Param("toYear") int toYear,
                                                         @Param("toMonth") int toMonth);


}
