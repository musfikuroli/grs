package com.grs.core.repo.grs;

import com.grs.core.domain.grs.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    @Query(nativeQuery = true, value = "select * from daily_report where office_id=:officeId and DATE_FORMAT(report_date, '%Y-%m-%d')=:reportDate")
    DailyReport findByOfficeIdAndDate(@Param("officeId") Long officeId, @Param("reportDate") String reportDate);
}
