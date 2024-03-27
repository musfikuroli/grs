package com.grs.core.repo.grs;

import com.grs.core.domain.grs.AttachedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 10/4/2017.
 */
@Repository
public interface AttachedFileRepo extends JpaRepository<AttachedFile, Long> {
}
