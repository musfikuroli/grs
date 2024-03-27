package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 9/6/2017.
 */
@Repository
public interface OfficeUnitOrganogramRepo extends JpaRepository<OfficeUnitOrganogram, Long> {
    List<OfficeUnitOrganogram> findByOfficeUnitId(Long officeUnitId);

    @Query(nativeQuery = true,
            value = "select * \n" +
                    "from office_unit_organograms\n" +
                    "where ref_origin_unit_org_id=:originUnitOrgId\n" +
                    "and office_id=:officeId\n")
    List<OfficeUnitOrganogram> findByOfficeOriginUnitOrgIdAndOfficeId(@Param("originUnitOrgId") Long originUnitOrgId, @Param("officeId") Long officeId);

    OfficeUnitOrganogram findByOfficeIdAndIsAdminAndStatus(Long officeId, Boolean isAdmin, Boolean status);
}
