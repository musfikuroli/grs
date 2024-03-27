package com.grs.core.repo.grs;

import com.grs.core.domain.grs.DashboardTotalResolved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardTotalResolvedRepo extends JpaRepository<DashboardTotalResolved, Integer> {
}
