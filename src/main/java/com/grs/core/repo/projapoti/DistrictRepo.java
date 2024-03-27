package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 02-Oct-17.
 */
@Repository
public interface DistrictRepo extends JpaRepository<District, Integer> {
    public List<District> findByDivisionId(Integer divisionId);
}
