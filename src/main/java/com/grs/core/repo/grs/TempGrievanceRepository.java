package com.grs.core.repo.grs;

import com.grs.core.domain.grs.TempGrievance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TempGrievanceRepository extends JpaRepository<TempGrievance, Long> {

    List<TempGrievance> findAllByStatus(String status);
}
