package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Occupation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
 * Created by HP on 1/30/2018.
 */
@Repository
public interface OccupationRepo extends JpaRepository<Occupation, Long> {
    public Page<Occupation> findByOrderByIdAsc(Pageable pageable);
    public Integer countByOccupationBanglaAndOccupationEnglish(String occupationNameBng, String occcupationNameEng);
    public List<Occupation> findByStatus(Boolean status);
}