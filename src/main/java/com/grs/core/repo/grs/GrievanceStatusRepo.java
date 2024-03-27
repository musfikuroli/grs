package com.grs.core.repo.grs;

import com.grs.core.domain.grs.GrievanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface GrievanceStatusRepo extends JpaRepository<GrievanceStatus, Long> {
    public GrievanceStatus findByStatusName(String statusName);
}
