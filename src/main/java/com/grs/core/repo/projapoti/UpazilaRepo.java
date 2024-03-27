package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.Upazila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 02-Oct-17.
 */
@Repository
public interface UpazilaRepo extends JpaRepository<Upazila, Integer> {
    List<Upazila> findByDistrictId(Integer districtId);
    List<Upazila> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId);
}
