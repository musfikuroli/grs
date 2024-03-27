package com.grs.core.repo.grs;


import com.grs.core.domain.grs.Tagid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagidRepo extends JpaRepository<Tagid, Long>{
    Page<Tagid> findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId, Pageable pageable);
}
