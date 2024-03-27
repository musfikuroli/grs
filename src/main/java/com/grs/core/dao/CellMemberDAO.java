package com.grs.core.dao;

import com.grs.core.domain.grs.CellMember;
import com.grs.core.repo.grs.CellMemberRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 11-Mar-18.
 */
@Service
public class CellMemberDAO {
    @Autowired
    private CellMemberRepo cellMemberRepo;

    public CellMember isACellMember(Long officeUnitOrganogramId) {
        return this.cellMemberRepo.findByCellOfficeUnitOrganogramId(officeUnitOrganogramId);
    }

    public CellMember findByOfficeIdAndEmployeeRecordId(Long officeId, Long officeUnitOrganogramId) {
        return this.cellMemberRepo.findByOfficeIdAndEmployeeRecordId(officeId, officeUnitOrganogramId);
    }

    public CellMember isACellMember(List<Long> officeIds, List<Long> officeUnitOrganogramIds) {
        return this.cellMemberRepo.findByOfficeIdInAndOfficeUnitOrganogramIdIn(officeIds, officeUnitOrganogramIds);
    }

    public Boolean isCellGRO(Long officeId, Long employeeRecordId) {
        CellMember cellMember = findByOfficeIdAndEmployeeRecordId(officeId, employeeRecordId);
        if(cellMember != null) {
            return cellMember.getIsGro();
        }
        return false;
    }

    public List<CellMember> getAllCellMembers() {
        return this.cellMemberRepo.findAll();
    }

    public Long getNextOrganogramId() {
        Long currentMaxId = this.cellMemberRepo.getMaxCellOfficeUnitOrganogramId();
        return (1L + (currentMaxId == null ? 0 : currentMaxId));
    }

    public CellMember save(CellMember cellMember) {
        cellMember.setStatus(true);
        return this.cellMemberRepo.save(cellMember);
    }

    public Boolean ifCellMemberAlreadyExists(Long cellMemberEmployeeRecordId) {
        return (this.cellMemberRepo.findByEmployeeRecordId(cellMemberEmployeeRecordId) != null);
    }

    public Boolean deleteOne(Long memberId) {
        CellMember cellMember = this.cellMemberRepo.findOne(memberId);
        if (cellMember.getIsGro() == true || cellMember.getIsAo() == true) {
            return false;
        }
        this.cellMemberRepo.delete(memberId);
        return true;
    }

    public CellMember findOne(Long memberId) {
        return this.cellMemberRepo.findOne(memberId);
    }

    public CellMember findByIsGro() {
        return this.cellMemberRepo.findByIsGro(true);
    }

    public CellMember findByIsAo() {
        return this.cellMemberRepo.findByIsAo(true);
    }
}
