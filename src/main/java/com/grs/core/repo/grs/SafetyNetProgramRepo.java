package com.grs.core.repo.grs;

import com.grs.core.domain.grs.SafetyNetProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyNetProgramRepo extends JpaRepository<SafetyNetProgram, Long> {
    List<SafetyNetProgram> findAllByActive(Boolean active);
    SafetyNetProgram findSafetyNetProgramByIdAndActive(Long id, Boolean active);

    @Query(nativeQuery = true, value = "select * from safety_net_program where (UPPER(name_en)=:nameEn or name_bn=:nameBn) and active=true")
    SafetyNetProgram findSafetyNetProgramByNameBnOrNameEn(@Param(value = "nameEn") String nameEn, @Param(value = "nameBn") String nameBn);

    @Query(value = "select * from safety_net_program where active=true", nativeQuery = true)
    List<SafetyNetProgram> findAllActive();

}
