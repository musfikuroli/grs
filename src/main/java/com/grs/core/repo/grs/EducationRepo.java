package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Education;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 1/31/2018.
 */
@Repository
public interface EducationRepo extends JpaRepository<Education, Long> {
    public Page<Education> findByOrderByIdAsc(Pageable pageable);
    public Integer countByEducationBanglaAndEducationEnglish(String educationNameBng, String educationNameEng);
    public List<Education> findByStatus(Boolean status);
}
