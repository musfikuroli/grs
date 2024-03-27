package com.grs.core.repo.grs;

import com.grs.core.domain.grs.GrsStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrsStatisticsRepo  extends JpaRepository<GrsStatistics,Integer> {

    GrsStatistics findByOfficeIdAndYearAndMonth(Long officeId, Integer year, Integer month);

}
