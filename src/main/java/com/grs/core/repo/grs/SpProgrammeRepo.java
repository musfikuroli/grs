package com.grs.core.repo.grs;

import com.grs.core.domain.grs.SpProgramme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Created on 22/8/2023.
 */
@Repository
public interface SpProgrammeRepo extends JpaRepository <SpProgramme, Integer> {
    public Optional<SpProgramme> findById(Integer id);
    public Integer countByNameEnOrNameBn(String nameEn, String nameBn);
    public List<SpProgramme> findAllByStatus(Boolean status);
}
