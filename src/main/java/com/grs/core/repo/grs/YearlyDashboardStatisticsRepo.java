package com.grs.core.repo.grs;

import com.grs.core.domain.grs.YearlyDashboardStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YearlyDashboardStatisticsRepo extends JpaRepository<YearlyDashboardStatistics, Integer> {
}
