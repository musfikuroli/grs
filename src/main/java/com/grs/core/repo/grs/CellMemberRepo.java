package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CellMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 11-Mar-18.
 */
@Repository
public interface CellMemberRepo extends JpaRepository<CellMember, Long> {
    CellMember findByOfficeIdAndCellOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId);

    CellMember findByCellOfficeUnitOrganogramId(Long officeUnitOrganogramId);

    CellMember findByOfficeIdAndEmployeeRecordId(Long officeId, Long employeeRecordId);

    CellMember findByOfficeIdInAndOfficeUnitOrganogramIdIn(List<Long> officeId, List<Long> officeUnitOrganogramId);

    @Query(nativeQuery = true,
            value = "SELECT MAX(cell_members.cell_office_unit_organogram_id)\n" +
                    "FROM cell_members")
    Long getMaxCellOfficeUnitOrganogramId();

    CellMember findByEmployeeRecordId(Long cellMemberEmployeeRecordId);

    CellMember findByIsGro(Boolean isGro);

    CellMember findByIsAo(Boolean isAo);
}
