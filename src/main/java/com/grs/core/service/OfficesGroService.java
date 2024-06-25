package com.grs.core.service;

import com.grs.api.model.response.OfficesGroDTO;
import com.grs.core.dao.OfficeLayerDAO;
import com.grs.core.dao.OfficesGroDAO;
import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.domain.RoleType;
import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.GrievanceForwarding;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Acer on 17-Dec-17.
 */
@Service
@Slf4j
public class OfficesGroService {
    @Autowired
    private OfficesGroDAO officesGroDAO;
    @Autowired
    private OfficeLayerDAO officeLayerDAO;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private GrievanceForwardingService grievanceForwardingService;
    @Autowired
    private GrievanceService grievanceService;

    @Autowired
    private BaseEntityManager baseEntityManager;

    public OfficesGRO findOfficesGroByOfficeId(Long id) {
        return this.officesGroDAO.findOfficesGROByOfficeId(id);
    }

    public List<OfficesGRO> findOfficesGroByMissingOfficerType(
            Long missingOfficerType,
            Long officeLayers,
            Long firstSelection,
            Long secondSelection
    ) {
        if (missingOfficerType == null) {
            log.error("invalid role type");
            return new ArrayList<>();
        }

        return this.officesGroDAO.findOfficesGroByMissingOfficerType(
                missingOfficerType,
                officeLayers,
                firstSelection,
                secondSelection
        );

    }

    public List<OfficesGRO> findByAppealOfficeUnitOrganogramId(Long officeUnitOrganogramId){
        return this.officesGroDAO.findByAppealOfficeUnitOrganogramId(officeUnitOrganogramId);
    }

    public List<OfficesGRO> findByAdminOfficeUnitOrganogramId(Long officeUnitOrganogramId){
        return this.officesGroDAO.findByAdminOfficeUnitOrganogramId(officeUnitOrganogramId);
    }

    @Transactional("transactionManager")
    public OfficesGRO save(OfficesGRO officesGRO) {
        return officesGroDAO.save(officesGRO);
    }

    public List<OfficesGRO> findAll() {
        return officesGroDAO.findAll();
    }

    public List<Long> findAllOffficeIds() {
        List<OfficesGRO> officeGros = this.officesGroDAO.findActiveOffices();
        return officeGros.stream()
                .map(OfficesGRO::getOfficeId)
                .sorted()
                .collect(Collectors.toList());
    }

    public Set<Long> findAllOffficeIdsIncludingInactive() {
        return this.officesGroDAO.findOfficeIds()
                .stream()
                .map(officeId -> officeId.longValue())
                .collect(Collectors.toSet());
    }

    public List<Long> findAllOffficeOriginIds() {
        List<OfficesGRO> officeGros = this.officesGroDAO.findAll();
        return officeGros.stream()
                .map(OfficesGRO::getOfficeOriginId)
                .sorted()
                .collect(Collectors.toList());
    }

    public OfficesGroDTO convertToOfficesGroDTO(OfficesGRO officesGRO) {
        return officesGroDAO.convertToOfficesGroDTO(officesGRO);
    }

    public List<Long> getGRSEnabledOfficeIdFromOfficeIdList(List<Long> officeIdList) {
        if (officeIdList == null || officeIdList.isEmpty()) return Collections.EMPTY_LIST;
        return officesGroDAO.getGRSEnabledOfficeIdFromOfficeIdList(officeIdList);
    }

    public List<Office> findByAppealOfficer(Long officeId, Long officeUnitOrganogramId) {
        if (officeId == 0) {
            List<OfficeLayer> officeLayers = this.officeService.getOfficeLayersByLayerLevel(1);
            return this.officeService.getOfficesByOfficeLayer(officeLayers, false);
        } else {
            List<OfficesGRO> officesGROES = this.officesGroDAO.findByAppealOfficeAndAppealOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
            return officesGROES.stream().map(
                    (OfficesGRO officesGRO) -> this.officeService.findOne(officesGRO.getOfficeId())
            )
                    .filter(officesGRO -> officesGRO != null)
                    .collect(Collectors.toList());
        }
    }

    public Page<OfficesGroDTO> findAll(Pageable pageable) {
        Page<OfficesGroDTO> officesGroDTOS = this.officesGroDAO.findAllOffices(pageable).map(this::convertToOfficesGroDTO);
        return officesGroDTOS;
    }

    public OfficesGroDTO findOne(Long id) {
        OfficesGRO officesGRO = this.officesGroDAO.findOne(id);
        return convertToOfficesGroDTO(officesGRO);
    }

    public void transferGrievancesOnGroChange(Long officeId, OfficesGRO old, OfficesGroDTO officesGroDTO) {
        //List<GrievanceForwarding> grievanceForwardings = this.grievanceForwardingService.getAllMovementsOfPreviousGRO(old);
        Long groOfficeId = old.getGroOfficeId();
        Long groOfficeUnitOrganogramId = old.getGroOfficeUnitOrganogramId();
        EmployeeOffice nextGroEmployeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGroDTO.getGroOfficeId(), officesGroDTO.getGroOfficeUnitOrganogramId(), true);
        if(nextGroEmployeeOffice != null) {
            String sql = "update complaint_movements set to_employee_record_id=:to_employee_record_id ," +
                    " to_employee_designation_bng=:to_employee_designation_bng," +
                    " to_employee_name_bng=:to_employee_name_bng, to_employee_name_eng=:to_employee_name_eng," +
                    " to_office_unit_organogram_id=:to_office_unit_organogram_id, to_office_unit_id=:to_office_unit_id," +
                    "modified_at=:modified_at where to_office_id=:to_office_id and is_current=1 and assigned_role='GRO' ";
            Map<String, Object> params = new HashMap<>();
            params.put("to_employee_record_id",nextGroEmployeeOffice.getEmployeeRecord().getId());
            params.put("to_employee_designation_bng", nextGroEmployeeOffice.getDesignation());
            params.put("to_employee_name_bng", nextGroEmployeeOffice.getEmployeeRecord().getNameBangla());
            params.put("to_employee_name_eng", nextGroEmployeeOffice.getEmployeeRecord().getNameEnglish());
            params.put("to_office_unit_organogram_id", officesGroDTO.getGroOfficeUnitOrganogramId());
            params.put("to_office_unit_id", nextGroEmployeeOffice.getOfficeUnit().getId());
            params.put("modified_at", new Date());
            params.put("to_office_id", groOfficeId);
            baseEntityManager.updateByQuery(sql, params);
            /*
            grievanceForwardings.forEach(
                    grievanceForwarding -> {
                        try {
                            if (Objects.equals(grievanceForwarding.getToOfficeId(), officeId) && Objects.equals(grievanceForwarding.getToOfficeUnitOrganogramId(), groOfficeUnitOrganogramId)) {
                                GrievanceForwarding grievanceForwarding1 = this.grievanceForwardingService.getGrievanceForwarding(
                                        grievanceForwarding.getGrievance(),
                                        "অনিক পরিবর্তিত হয়েছে",
                                        "GRO_CHANGED",
                                        nextGroEmployeeOffice.getEmployeeRecord().getId(),
                                        grievanceForwarding.getToEmployeeRecordId(),
                                        officesGroDTO.getGroOfficeId(),
                                        groOfficeId,
                                        officesGroDTO.getGroOfficeUnitOrganogramId(),
                                        groOfficeUnitOrganogramId,
                                        true,
                                        false,
                                        false,
                                        false,
                                        null,
                                        grievanceForwarding.getGrievance().getGrievanceCurrentStatus(),
                                        RoleType.GRO,
                                        ""
                                );
                                this.grievanceForwardingService.forwardRemovingFromInbox(grievanceForwarding1);
                            } else if (Objects.equals(grievanceForwarding.getFromOfficeId(), groOfficeId) && Objects.equals(grievanceForwarding.getFromOfficeUnitOrganogramId(), groOfficeUnitOrganogramId)) {
                                GrievanceForwarding grievanceForwarding1 = this.grievanceForwardingService.getGrievanceForwarding(
                                        grievanceForwarding.getGrievance(),
                                        "অনিক পরিবর্তিত হয়েছে",
                                        "GRO_CHANGED",
                                        grievanceForwarding.getToEmployeeRecordId(),
                                        nextGroEmployeeOffice.getEmployeeRecord().getId(),
                                        grievanceForwarding.getToOfficeId(),
                                        officesGroDTO.getGroOfficeId(),
                                        grievanceForwarding.getToOfficeUnitOrganogramId(),
                                        officesGroDTO.getGroOfficeUnitOrganogramId(),
                                        true,
                                        false,
                                        false,
                                        false,
                                        null,
                                        grievanceForwarding.getGrievance().getGrievanceCurrentStatus(),
                                        RoleType.GRO,
                                        ""
                                );
                                this.grievanceForwardingService.forwardRemovingFromInbox(grievanceForwarding1);
                            } else {
                                GrievanceForwarding grievanceForwarding2 = this.grievanceForwardingService.getGrievanceForwarding(
                                        grievanceForwarding.getGrievance(),
                                        "অনিক পরিবর্তিত হয়েছে",
                                        "GRO_CHANGED",
                                        nextGroEmployeeOffice.getEmployeeRecord().getId(),
                                        grievanceForwarding.getToEmployeeRecordId(),
                                        officeId,
                                        officesGroDTO.getGroOfficeId(),
                                        officesGroDTO.getGroOfficeUnitOrganogramId(),
                                        groOfficeUnitOrganogramId,
                                        true,
                                        false,
                                        false,
                                        false,
                                        null,
                                        grievanceForwarding.getGrievance().getGrievanceCurrentStatus(),
                                        RoleType.GRO,
                                        ""
                                );
                                this.grievanceForwardingService.forwardRemovingFromInbox(grievanceForwarding2);
                            }
                        } catch (Exception ex) {
                            log.info(ex.getMessage());
                            GrievanceForwarding grievanceForwarding1 = this.grievanceForwardingService.getGrievanceForwarding(
                                    grievanceForwarding.getGrievance(),
                                    "অনিক পরিবর্তিত হয়েছে",
                                    "GRO_CHANGED",
                                    nextGroEmployeeOffice.getEmployeeRecord().getId(),
                                    nextGroEmployeeOffice.getEmployeeRecord().getId(),
                                    officesGroDTO.getGroOfficeId(),
                                    officesGroDTO.getGroOfficeId(),
                                    officesGroDTO.getGroOfficeUnitOrganogramId(),
                                    officesGroDTO.getGroOfficeUnitOrganogramId(),
                                    true,
                                    false,
                                    false,
                                    false,
                                    null,
                                    grievanceForwarding.getCurrentStatus(),
                                    RoleType.GRO,
                                    ""
                            );
                            Grievance grievance = grievanceForwarding.getGrievance();
                            this.grievanceForwardingService.forwardRemovingFromInbox(grievanceForwarding1);
                            grievanceService.saveGrievance(grievance);
                        }
                    }
            );

             */
        }
    }

    public void changeChildOfficesAoOnGroChange(OfficesGroDTO officesGroDTO) {
        List<Office> childOfficeList = officeService.getOfficesByParentOfficeId(officesGroDTO.getGroOfficeId());
        for(Office childOffice: childOfficeList) {
            OfficesGRO childOfficesGRO = officesGroDAO.findByOfficeId(childOffice.getId());
            if(childOfficesGRO != null) {
                childOfficesGRO.setAppealOfficeId(officesGroDTO.getGroOfficeId());
                childOfficesGRO.setAppealOfficerOfficeUnitOrganogramId(officesGroDTO.getGroOfficeUnitOrganogramId());
                officesGroDAO.save(childOfficesGRO);
            } else {
                Office office = this.officeService.getOffice(childOffice.getId());
                CustomOfficeLayer customOfficeLayer = office == null || office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : this.officeLayerDAO.getCustomOfficeLayerById(office.getOfficeLayer().getCustomLayerId().longValue());
                OfficesGRO officesGRO = OfficesGRO.builder()
                        .officeId(childOffice.getId())
                        .officeNameBangla(childOffice.getNameBangla())
                        .officeNameEnglish(childOffice.getNameEnglish())
                        .officeOriginId(childOffice.getOfficeOriginId())
                        .appealOfficeId(officesGroDTO.getGroOfficeId())
                        .appealOfficerOfficeUnitOrganogramId(officesGroDTO.getGroOfficeUnitOrganogramId())
                        .aoOfficeUnitName(officesGroDTO.getGroOfficeUnitName())
                        .build();

                officesGRO.setLayerLevel(office.getOfficeLayer() == null || office.getOfficeLayer().getLayerLevel() == null ? null : office.getOfficeLayer().getLayerLevel());
                officesGRO.setCustomLayerLevel(customOfficeLayer == null || customOfficeLayer.getLayerLevel() == null ? null : customOfficeLayer.getLayerLevel());
                officesGRO.setCustomLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : office.getOfficeLayer().getCustomLayerId());
                officesGRO.setOfficeLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getId() == null ? null : office.getOfficeLayer().getId());
                officesGRO.setOfficeOriginId(office.getOfficeOriginId() == null ? null : office.getOfficeOriginId());
                officesGRO.setOfficeMinistryId(office.getOfficeMinistry() == null || office.getOfficeMinistry().getId() == null ? null : office.getOfficeMinistry().getId());

                officesGroDAO.save(officesGRO);
            }
        }
    }

    @Transactional(value = "transactionManager", rollbackFor = RuntimeException.class)
    public OfficesGroDTO saveOfficeSetup(Long officeId, OfficesGroDTO officesGroDTO) {
        OfficesGRO officesGROOld = officesGroDAO.findByOfficeId(officeId);
        Office office = this.officeService.getOffice(officeId);
        CustomOfficeLayer customOfficeLayer = office == null || office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : this.officeLayerDAO.getCustomOfficeLayerById(office.getOfficeLayer().getCustomLayerId().longValue());
        if (officesGROOld == null) {
            officesGROOld = OfficesGRO.builder().build();
        } else {
            if(officesGroDTO.getGroOfficeId() != null && officesGroDTO.getGroOfficeUnitOrganogramId() != null){//GRO is being changed
                try {
                    this.transferGrievancesOnGroChange(officeId, officesGROOld, officesGroDTO);
                    this.changeChildOfficesAoOnGroChange(officesGroDTO);
                    if (office != null && Objects.equals(new Long(office.getOfficeLayer().getLayerLevel()), Constant.districtLayerLevel)) {
                        this.setUpazilaOffices(officesGroDTO.getGroOfficeUnitName(), officesGroDTO.getGroOfficeId(), officesGroDTO.getGroOfficeUnitOrganogramId());
                    }
                } catch (Throwable t) {
                    throw new RuntimeException("Internal service error. Please contact with admin");
                }
            }
        }

        officesGROOld.setOfficeId(officesGroDTO.getOfficeId() == null ? officesGROOld.getOfficeId() : officesGroDTO.getOfficeId());
        officesGROOld.setOfficeOriginId(officesGroDTO.getOfficeOriginId() == null ? officesGROOld.getOfficeOriginId() : officesGroDTO.getOfficeOriginId());
        officesGROOld.setGroOfficeId(officesGroDTO.getGroOfficeId() == null ? officesGROOld.getGroOfficeId() : officesGroDTO.getGroOfficeId());
        officesGROOld.setGroOfficeUnitOrganogramId(officesGroDTO.getGroOfficeUnitOrganogramId() == null ? officesGROOld.getGroOfficeUnitOrganogramId() : officesGroDTO.getGroOfficeUnitOrganogramId());
        officesGROOld.setAppealOfficeId(officesGroDTO.getAppealOfficeId() == null ? officesGROOld.getAppealOfficeId() : officesGroDTO.getAppealOfficeId());
        officesGROOld.setAppealOfficerOfficeUnitOrganogramId(officesGroDTO.getAppealOfficerOfficeUnitOrganogramId() == null ? officesGROOld.getAppealOfficerOfficeUnitOrganogramId() : officesGroDTO.getAppealOfficerOfficeUnitOrganogramId());
        officesGROOld.setAdminOfficeId(officesGroDTO.getAdminOfficeId() == null ? officesGROOld.getAdminOfficeId() : officesGroDTO.getAdminOfficeId());
        officesGROOld.setAdminOfficeUnitOrganogramId(officesGroDTO.getAdminOfficeUnitOrganogramId() == null ? officesGROOld.getAdminOfficeUnitOrganogramId() : officesGroDTO.getAdminOfficeUnitOrganogramId());
        officesGROOld.setOfficeNameBangla(officesGroDTO.getOfficeName() == null ? officesGROOld.getOfficeNameBangla() : officesGroDTO.getOfficeName());
        officesGROOld.setGroOfficeUnitName(officesGroDTO.getGroOfficeUnitName() == null ? officesGROOld.getGroOfficeUnitName() : officesGroDTO.getGroOfficeUnitName());
        officesGROOld.setAoOfficeUnitName(officesGroDTO.getAoOfficeUnitName() == null ? officesGROOld.getAoOfficeUnitName() : officesGroDTO.getAoOfficeUnitName());
        officesGROOld.setAdminOfficeUnitName(officesGroDTO.getAdminOfficeUnitName() == null ? officesGROOld.getAdminOfficeUnitName() : officesGroDTO.getAdminOfficeUnitName());

        officesGROOld.setLayerLevel(office != null && office.getOfficeLayer() != null ? office.getOfficeLayer().getLayerLevel() : null);
        officesGROOld.setCustomLayerLevel(customOfficeLayer != null ? customOfficeLayer.getLayerLevel() : null);
        officesGROOld.setCustomLayerId(office != null && office.getOfficeLayer() != null ? office.getOfficeLayer().getCustomLayerId() : null);
        officesGROOld.setOfficeLayerId(office == null || office.getOfficeLayer() == null || office.getOfficeLayer().getId() == null ? null : office.getOfficeLayer().getId());
        officesGROOld.setOfficeOriginId(office == null || office.getOfficeOriginId() == null ? null : office.getOfficeOriginId());
        officesGROOld.setOfficeMinistryId(office == null || office.getOfficeMinistry() == null || office.getOfficeMinistry().getId() == null ? null : office.getOfficeMinistry().getId());
        try {
            OfficesGRO officesGRO = this.officesGroDAO.save(officesGROOld);
            if (officesGRO == null) {
                throw new RuntimeException("Internal service error. Please contact with admin");
            }
            return convertToOfficesGroDTO(officesGRO);
        } catch (Throwable t) {
            throw new RuntimeException("Internal service error. Please contact with admin");
        }
    }

    private void setUpazilaOffices(String groOfficeUnitName, Long groOfficeId, Long groOfficeUnitOrganogramId) {
        List<Office> childOfficeList = officeService.getOfficesByParentOfficeId(groOfficeId);
        for(Office childOffice: childOfficeList) {
            OfficesGRO childOfficesGRO = officesGroDAO.findByOfficeId(childOffice.getId());
            if(childOfficesGRO != null) {
                childOfficesGRO.setAppealOfficeId(groOfficeId);
                childOfficesGRO.setAppealOfficerOfficeUnitOrganogramId(groOfficeUnitOrganogramId);

                childOfficesGRO.setGroOfficeId(groOfficeId);
                childOfficesGRO.setGroOfficeUnitOrganogramId(groOfficeUnitOrganogramId);
                officesGroDAO.save(childOfficesGRO);
            } else {
                Office office = this.officeService.getOffice(childOffice.getId());
                CustomOfficeLayer customOfficeLayer = office == null || office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : this.officeLayerDAO.getCustomOfficeLayerById(office.getOfficeLayer().getCustomLayerId().longValue());
                OfficesGRO officesGRO = OfficesGRO.builder()
                        .officeId(childOffice.getId())
                        .officeNameBangla(childOffice.getNameBangla())
                        .officeNameEnglish(childOffice.getNameEnglish())
                        .officeOriginId(childOffice.getOfficeOriginId())
                        .appealOfficeId(groOfficeId)
                        .appealOfficerOfficeUnitOrganogramId(groOfficeUnitOrganogramId)
                        .aoOfficeUnitName(groOfficeUnitName)
                        .groOfficeId(groOfficeId)
                        .groOfficeUnitOrganogramId(groOfficeUnitOrganogramId)
                        .groOfficeUnitName(groOfficeUnitName)
                        .build();

                officesGRO.setLayerLevel(office.getOfficeLayer() == null || office.getOfficeLayer().getLayerLevel() == null ? null : office.getOfficeLayer().getLayerLevel());
                officesGRO.setCustomLayerLevel(customOfficeLayer == null || customOfficeLayer.getLayerLevel() == null ? null : customOfficeLayer.getLayerLevel());
                officesGRO.setCustomLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : office.getOfficeLayer().getCustomLayerId());
                officesGRO.setOfficeLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getId() == null ? null : office.getOfficeLayer().getId());
                officesGRO.setOfficeOriginId(office.getOfficeOriginId() == null ? null : office.getOfficeOriginId());
                officesGRO.setOfficeMinistryId(office.getOfficeMinistry() == null || office.getOfficeMinistry().getId() == null ? null : office.getOfficeMinistry().getId());

                officesGroDAO.save(officesGRO);
            }
        }
    }

    public Page<OfficesGroDTO> findActiveSetup(Pageable pageable) {
        Page<OfficesGroDTO> officesGroDTOS = this.officesGroDAO.findActiveOffices(pageable).map(this::convertToOfficesGroDTO);
        return officesGroDTOS;
    }

    public OfficesGroDTO findOfficersByOfficeId(Long officeId) {
        OfficesGRO officesGRO = this.findOfficesGroByOfficeId(officeId);
        OfficesGroDTO officesGroDTO = new OfficesGroDTO();
        Office office;
        OfficeUnitOrganogram officeUnitOrganogram;
        EmployeeOffice employeeOffice;
        EmployeeRecord employeeRecord = null;

        if (officesGRO != null) {
            if (officesGRO.getGroOfficeId() != null) {
                office = this.officeService.getOffice(officesGRO.getGroOfficeId());
                officesGroDTO.setGroOfficeName(office.getNameBangla());
                officeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(officesGRO.getGroOfficeUnitOrganogramId());
                officesGroDTO.setGroDesignation(officeUnitOrganogram.getDesignationBangla());
                employeeOffice = this.officeService.findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId(officesGRO.getGroOfficeId(), officesGRO.getGroOfficeUnitOrganogramId());
                if (employeeOffice != null) {
                    employeeRecord = this.officeService.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                    officesGroDTO.setGroName(employeeRecord.getNameBangla());
                    officesGroDTO.setGroPhone(employeeRecord.getPersonalMobile());
                } else {
                    officesGroDTO.setGroName("এই পদের কর্মকর্তার তথ্য ত্রুটিযুক্ত");
                    officesGroDTO.setGroPhone("-");
                }
                officesGroDTO.setGroOfficeUnitName(officesGRO.getGroOfficeUnitName());
            } else {
                officesGroDTO.setGroName("এই পদে কর্মকর্তা নেই");
                officesGroDTO.setGroDesignation("-");
                officesGroDTO.setGroOfficeUnitName("-");
                officesGroDTO.setGroOfficeName("-");
                officesGroDTO.setGroPhone("-");
            }
            if (officesGRO.getAppealOfficeId() != null) {
                office = this.officeService.getOffice(officesGRO.getAppealOfficeId());
                officesGroDTO.setAoOfficeName(office.getNameBangla());
                officesGroDTO.setAppealOfficeId(officesGRO.getAppealOfficeId());
                officeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(officesGRO.getAppealOfficerOfficeUnitOrganogramId());
                officesGroDTO.setAoDesignation(officeUnitOrganogram.getDesignationBangla());
                employeeOffice = this.officeService.findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId(officesGRO.getAppealOfficeId(), officesGRO.getAppealOfficerOfficeUnitOrganogramId());
                if (employeeOffice != null) {
                    employeeRecord = this.officeService.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                    officesGroDTO.setAoName(employeeRecord.getNameBangla());
                    officesGroDTO.setAoPhone(employeeRecord.getPersonalMobile());
                } else {
                    officesGroDTO.setAoName("এই পদের কর্মকর্তার তথ্য ত্রুটিযুক্ত");
                    officesGroDTO.setAoPhone("-");
                }
                officesGroDTO.setAoOfficeUnitName(officesGRO.getAoOfficeUnitName());
            } else {
                officesGroDTO.setAoName("এই পদে কর্মকর্তা নেই");
                officesGroDTO.setAoDesignation("-");
                officesGroDTO.setAoOfficeUnitName("-");
                officesGroDTO.setAoOfficeName("-");
                officesGroDTO.setAoPhone("-");
            }
            if (officesGRO.getAdminOfficeId() != null) {
                office = this.officeService.getOffice(officesGRO.getAdminOfficeId());
                officesGroDTO.setAdminOfficeName(office.getNameBangla());
                officeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(officesGRO.getAdminOfficeUnitOrganogramId());
                officesGroDTO.setAdminDesignation(officeUnitOrganogram.getDesignationBangla());
                employeeOffice = this.officeService.findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId(officesGRO.getAdminOfficeId(), officesGRO.getAdminOfficeUnitOrganogramId());
                if (employeeOffice != null) {
                    employeeRecord = this.officeService.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                    officesGroDTO.setAdminName(employeeRecord.getNameBangla());
                    officesGroDTO.setAdminPhone(employeeRecord.getPersonalMobile());
                } else {
                    officesGroDTO.setAdminName("এই পদের কর্মকর্তার তথ্য ত্রুটিযুক্ত");
                    officesGroDTO.setAdminPhone("-");
                }
                officesGroDTO.setAdminOfficeUnitName(officesGRO.getAdminOfficeUnitName());
            } else {
                officesGroDTO.setAdminName("এই পদে কর্মকর্তা নেই");
                officesGroDTO.setAdminDesignation("-");
                officesGroDTO.setAdminOfficeUnitName("-");
                officesGroDTO.setAdminOfficeName("");
                officesGroDTO.setAdminPhone("-");
            }
        } else {
            officesGroDTO.setGroName("এই পদে কর্মকর্তা নেই");
            officesGroDTO.setGroDesignation("-");
            officesGroDTO.setGroOfficeUnitName("-");
            officesGroDTO.setGroOfficeName("-");
            officesGroDTO.setGroPhone("-");
            officesGroDTO.setAoName("এই পদে কর্মকর্তা নেই");
            officesGroDTO.setAoDesignation("-");
            officesGroDTO.setAoOfficeUnitName("-");
            officesGroDTO.setAoOfficeName("-");
            officesGroDTO.setAoPhone("-");
            officesGroDTO.setAdminName("এই পদে কর্মকর্তা নেই");
            officesGroDTO.setAdminDesignation("-");
            officesGroDTO.setAdminOfficeUnitName("-");
            officesGroDTO.setAdminOfficeName("-");
            officesGroDTO.setAdminPhone("-");
        }
        return officesGroDTO;
    }

    List<OfficesGRO> getCurrentlyGrsEnabledOffices() {
        List<OfficesGRO> officesGROList = officesGroDAO.findAll();
        officesGROList = officesGROList.stream().filter(officesGRO -> {
            return officesGRO.getGroOfficeId()!= null && officesGRO.getGroOfficeUnitOrganogramId()!=null && officesGRO.getAppealOfficeId()!=null && officesGRO.getAppealOfficerOfficeUnitOrganogramId()!=null;
        }).collect(Collectors.toList());
        return officesGROList;
    }

    List<OfficesGRO> getAncestors(Long officeId){
        List<OfficesGRO> officesGROES = new ArrayList<>();
        OfficesGRO officesGRO = this.officesGroDAO.findByOfficeId(officeId);
        officesGROES.add(officesGRO);
        int count = 0;
        while (!Objects.equals(officesGRO.getGroOfficeId(), officesGRO.getAppealOfficeId())){
            Long appealOfficeId = officesGRO.getAppealOfficeId();
            officesGRO = this.officesGroDAO.findByOfficeId(appealOfficeId);
            officesGROES.add(officesGRO);
            count++;
            if(count > 30){
                break;
            }
        }
        return officesGROES;
    }

    public boolean disableOfficeSetup(Long officeId) {
        OfficesGRO officesGRO = this.officesGroDAO.findByOfficeId(officeId);
        officesGRO.setGroOfficeId(null);
        officesGRO.setGroOfficeUnitOrganogramId(null);
        officesGRO = this.officesGroDAO.save(officesGRO);
        return officesGRO != null;
    }
}
