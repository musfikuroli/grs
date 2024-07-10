package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ComplainHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplainHistoryRepository extends JpaRepository<ComplainHistory, Long> {
}
