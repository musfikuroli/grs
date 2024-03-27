package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.Thana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 17-Oct-17.
 */
@Repository
public interface ThanaRepo extends JpaRepository<Thana, Integer> {
    public List<Thana> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId);
    public List<Thana> findByDivisionId(Integer divisionId);
}