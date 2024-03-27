package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.OfficeMinistry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface OfficeMinistryRepo extends JpaRepository<OfficeMinistry, Long> {

}
