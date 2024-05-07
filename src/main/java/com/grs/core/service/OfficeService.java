package com.grs.core.service;

import com.grs.api.config.security.TokenAuthenticationServiceUtil;
import com.grs.api.model.*;
import com.grs.api.model.oisf_response.DTOConverter;
import com.grs.api.model.request.KeyValueStringPairDTO;
import com.grs.api.model.response.*;
import com.grs.api.model.response.dashboard.GrievanceCountByItemDTO;
import com.grs.api.model.response.officeSelection.DropDownItemDTO;
import com.grs.api.model.response.officeSelection.OfficeSearchContentsDTO;
import com.grs.api.model.response.officeSelection.OfficeSearchDTO;
import com.grs.api.model.response.roles.RoleContainerDTO;
import com.grs.api.model.response.roles.SingleRoleDTO;
import com.grs.core.dao.*;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.*;
import com.grs.core.domain.projapoti.*;
import com.grs.core.repo.projapoti.EmployeeOfficeRepo;
import com.grs.core.repo.projapoti.EmployeeRecordRepo;
import com.grs.utils.CacheUtil;
import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Acer on 8/30/2017.
 */
@Slf4j
@Service
public class OfficeService {
    @Autowired
    private OfficeLayerDAO officeLayerDAO;
    @Autowired
    private OfficeDAO officeDAO;
    @Autowired
    private ServiceOriginDAO serviceOriginDAO;
    @Autowired
    private EmployeeOfficeRepo employeeOfficeRepo;
    @Autowired
    private EmployeeRecordRepo employeeRecordRepo;
    @Autowired
    private EmployeeOfficeDAO employeeOfficeDAO;
    @Autowired
    private CitizenCharterDAO citizenCharterDAO;
    @Autowired
    private EmployeeRecordDAO employeeRecordDAO;
    @Autowired
    private OfficeUnitOrganogramService officeUnitOrganogramService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private OfficeUnitDAO officeUnitDAO;
    @Autowired
    private MessageService messageService;
    @Autowired
    private CellService cellService;
    @Autowired
    private OfficeOriginDAO officeOriginDAO;
    @Autowired
    private OfficeOrganogramService officeOrganogramService;
    @Autowired
    private CentralDashboardRecipientDAO centralDashboardRecipientDAO;

    public Integer getChildCountByParentOfficeId(Long parentOfficeId) {
        return this.officeDAO.getChildCountByParentOfficeId(parentOfficeId);
    }

    public Office findOne(Long id) {
        if (id == 0) {
            OfficeMinistry officeMinistry = OfficeMinistry.builder()
                    .id(0L)
                    .build();

            OfficeLayer officeLayer = OfficeLayer.builder()
                    .id(0L)
                    .layerLevel(6)
                    .build();

            return Office.builder()
                    .id(0L)
                    .officeMinistry(officeMinistry)
                    .officeLayer(officeLayer)
                    .nameEnglish("Cell")
                    .nameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                    .build();
        }
        return this.officeDAO.findOne(id);
    }

    public EmployeeOffice findEmployeeOfficeByOfficeAndIsOfficeHead(Long officeId) {
        EmployeeOffice employeeOffice = this.employeeOfficeDAO.findEmployeeOfficeByOfficeAndIsOfficeHead(officeId);
        if (employeeOffice == null) {
            log.error("EmployeeOffice object is null from findEmployeeOfficeByOfficeAndIsOfficeHead");
            return null;
        }
        return employeeOffice;
    }

    public EmployeeOffice findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(Long officeId, Long officeUnitOrganogramId, boolean status) {
        EmployeeOffice employeeOffice;
        CellMember cellMember = null;
        if (officeId == 0L) {
            cellMember = this.cellService.getCellMemberEntry(officeUnitOrganogramId);
            employeeOffice = this.employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(cellMember.getOfficeId(), cellMember.getOfficeUnitOrganogramId());
        } else {
            employeeOffice = this.employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
        }
        if (employeeOffice == null) {
            log.error("EmployeeOffice object is null from findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus");
        }
        if (cellMember != null) {
            Office cellOffice = this.findOne(0L);
            OfficeUnit officeUnit = this.getOfficeUnitByIdIncludingFakeOfficeUnitForCell(0L);
            OfficeUnitOrganogram officeUnitOrganogram = this.getOfficeUnitOrganogramById(cellMember.getCellOfficeUnitOrganogramId());
            officeUnitOrganogram.setDesignationBangla(cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য"));
            officeUnitOrganogram.setDesignationEnglish(cellMember.getIsGro() ? "President" : (cellMember.getIsGro() ? "Member Secretary" : "Member"));

            employeeOffice = EmployeeOffice.builder()
                    .employeeRecord(employeeOffice == null ? EmployeeRecord.builder().build() : employeeOffice.getEmployeeRecord())
                    .designation(cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য"))
                    .office(cellOffice)
                    .officeUnit(officeUnit)
                    .officeUnitOrganogram(officeUnitOrganogram)
                    .build();
        }
        if (employeeOffice == null) {
            log.error("EmployeeOffice object is null from findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus");
            return null;
        }
        return employeeOffice;

    }

    public Office getOffice(Long officeId) {
        return this.findOne(officeId);
    }

    public OfficeOrigin getOfficeOrigin(Long officeOriginId) {
        return officeOriginDAO.findOfficeOriginById(officeOriginId);
    }

    public ServiceOrigin getServiceOrigin(Long id) {
        return serviceOriginDAO.findOne(id);
    }

    public EmployeeRecord findEmployeeRecordById(Long id) {
        return this.employeeRecordDAO.findEmployeeRecordById(id);
    }

    public SingleRoleDTO findSingleRole(Long officeId, Long officeUnitOrganogramId) {
        return this.employeeOfficeDAO.findSingleRole(officeId, officeUnitOrganogramId);
    }

    public List<OfficeLayer> getOfficeLayersByLayerLevel(Integer layerLevel) {
        return this.officeLayerDAO.getOfficeLayersIdByLayerLevel(layerLevel);
    }

    public List<OfficeLayer> getOfficeLayersByLayerLevelAndMinistryId(Integer layerLevel, Long ministryId) {
        return this.officeLayerDAO.getOfficeLayersByLayerLevelAndMinistryId(layerLevel, ministryId);
    }

    public List<OfficeLayer> getOfficeLayersByLayerLevelAndCustomLayerId(Integer layerLevel, Integer customLayerId) {
        return this.officeLayerDAO.getOfficeByLayerLevelAndCustomLayerId(layerLevel, customLayerId);
    }

    public List<OfficeLayer> getOfficeLayersByLayerLevelAndCustomLayerIdInList(Integer layerLevel, List<Integer> customLayerIdList) {
        return this.officeLayerDAO.getOfficeByLayerLevelAndCustomLayerIdInList(layerLevel, customLayerIdList);
    }

    public Boolean isMinistryOrDivisionLevelOffice(Long officeId) {
        Office office = officeDAO.findOne(officeId);
        OfficeLayer officeLayer = office.getOfficeLayer();
        if (officeLayer != null) {
            return officeLayer.getLayerLevel() == 1;
        } else {
            return false;
        }
    }

    public List<Office> getOfficesByOfficeLayer(List<OfficeLayer> officeLayers, Boolean grsEnabled) {
        List<Office> offices = this.officeDAO.getOfficesByOfficeLayer(officeLayers);
        if (grsEnabled) {
            List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
            return offices.stream()
                    .filter(office -> officeIdsInOfficesGro.contains(office.getId()))
                    .collect(Collectors.toList());

        } else {
            return offices;
        }
    }

    public String getOfficeName(long id) {
        return officeDAO.findOne(id).getNameBangla();
    }

    public List<Office> getOfficesByParentOfficeId(Long parentOfficeId) {
        return officeDAO.getOfficesByParentOfficeId(parentOfficeId);
    }

    public List<Office> findByOfficeIdInList(List<Long> idList) {
        return officeDAO.findByOfficeIdInList(idList);
    }


    public ServiceOriginDTO convertToService(CitizenCharter citizenCharter) {
        EmployeeOffice employeeOffice = this.findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId(citizenCharter.getSoOfficeId(), citizenCharter.getSoOfficeUnitOrganogramId());
        List<EmployeeRecordDTO> responsible;
        if (employeeOffice == null) {
            log.info("EmployeeOffice object is null from convertToService");
            responsible = Arrays.asList(employeeOfficeDAO.getEmployeeRecordDTO(null));
        } else {
            responsible = Arrays.asList(employeeOfficeDAO.getEmployeeRecordDTO(employeeOffice));
        }

        return ServiceOriginDTO.builder()
                .id(citizenCharter.getId())
                .serviceId(citizenCharter.getServiceOrigin().getId())
                .serviceNameBangla(citizenCharter.getServiceNameBangla())
                .serviceNameEnglish(citizenCharter.getServiceNameEnglish())
                .serviceProcedureBangla(citizenCharter.getServiceProcedureBangla())
                .serviceProcedureEnglish(citizenCharter.getServiceProcedureEnglish())
                .documentAndLocationBangla(citizenCharter.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(citizenCharter.getDocumentAndLocationEnglish())
                .paymentMethodBangla(citizenCharter.getPaymentMethodBangla())
                .paymentMethodEnglish(citizenCharter.getPaymentMethodEnglish())
                .serviceTime(citizenCharter.getServiceTime())
                .responsible(responsible)
                .status(citizenCharter.getStatus())
                .build();
    }

    public List<CitizenCharter> getServicesHavingServiceOfficerInfo(List<CitizenCharter> citizenCharters) {
        return citizenCharters.stream().filter(cc -> {
            return (cc.getSoOfficeId() != null && cc.getSoOfficeUnitId() != null && cc.getOfficeOriginId() != null);
        }).collect(Collectors.toList());
    }

    public List<ServiceOriginDTO> getServices(Long officeID) {
        List<CitizenCharter> citizenCharters = this.citizenCharterDAO.findByOffice(officeID);
        return citizenCharters.stream()
                .map(this::convertToService)
                .collect(Collectors.toList());
    }

    public List<ServiceOriginDTO> getServicesByServiceType(Long officeID, ServiceType serviceType) {
        List<CitizenCharter> citizenCharters = this.citizenCharterDAO.findByOfficeAndServiceOriginServiceType(officeID, serviceType);
        citizenCharters = getServicesHavingServiceOfficerInfo(citizenCharters);
        return citizenCharters.stream()
                .map(this::convertToService)
                .collect(Collectors.toList());
    }

    public List<ServiceOriginDTO> getServicesByServiceTypeFromOfficesCitizenCharter(Long officeId, ServiceType serviceType) {
        List<CitizenCharter> officesCitizenCharters = citizenCharterDAO.findByOfficeAndServiceOriginServiceType(officeId, serviceType);
        return officesCitizenCharters.stream()
                .map(this::convertToService).map(serviceOriginDTO -> {
                    serviceOriginDTO.setServiceType(serviceType);
                    return serviceOriginDTO;
                })
                .collect(Collectors.toList());
    }

    public BaseObjectContainerDTO getEmployeesByOffices(Long officeID) {
        Office office = this.officeDAO.findOne(officeID);
        List<EmployeeRecordDTO> employeeRecordDTOs = employeeRecordDAO.findAllByOffice(office);
        List<BaseObjectDTO> employeeInfoDTOs = new ArrayList<>();
        employeeRecordDTOs.forEach(employeeRecordDTO -> {
            List<OfficeUnitWithDesignationDTO> unitWithDesignations = employeeRecordDTO.getOfficeUnitWithDesignations();
            if (unitWithDesignations.size() > 0) {
                unitWithDesignations.forEach(unitWithDesignation -> {
                    String phoneNumber = employeeRecordDTO.getPhoneNumber();
                    String name = employeeRecordDTO.getName()
                            + (StringUtil.isValidString(phoneNumber) ? (" (" + phoneNumber + "), ") : "")
                            + unitWithDesignation.getDesignation() + ", "
                            + unitWithDesignation.getOfficeUnitNameBangla();
                    BaseObjectDTO baseObjectDTO = new BaseObjectDTO();
                    baseObjectDTO.setId(unitWithDesignation.getOfficeUnitOrganogramId());
                    baseObjectDTO.setName(name);
                    employeeInfoDTOs.add(baseObjectDTO);
                });
            }
        });

        return BaseObjectContainerDTO.builder()
                .objects(employeeInfoDTOs)
                .build();
    }

    public List<EmployeeRecordDTO> getListOfEmployeeRecordsFromGivenOfficeUnitOrganogram(Long officeUnitOrganogramId) {
        OfficeUnitOrganogram officeUnitOrganogram = this.getOfficeUnitOrganogramById(officeUnitOrganogramId);
        OfficeUnit officeUnit = officeUnitOrganogram.getOfficeUnit();
        List<EmployeeOffice> employeeOffices = employeeOfficeRepo.findByStatusAndOfficeUnitOrganogram(true, officeUnitOrganogram);
        List<EmployeeRecord> employeeRecords = employeeRecordRepo.findByEmployeeOfficesIn(employeeOffices);
        Boolean isEnglish = messageService.isCurrentLanguageInEnglish();
        List<EmployeeRecordDTO> employeeRecordDTOS = employeeRecords.stream().map(employeeRecord -> {
            String designation = isEnglish ? officeUnitOrganogram.getDesignationEnglish() : officeUnitOrganogram.getDesignationBangla();
            designation += ((officeUnit != null && designation.trim().length() > 0) ? ", " : "");
            designation += officeUnit == null ? "" : (isEnglish ? officeUnit.getUnitNameEnglish() : officeUnit.getUnitNameBangla());
            return EmployeeRecordDTO.builder()
                    .id(String.valueOf(employeeRecord.getId()))
                    .name(isEnglish ? employeeRecord.getNameEnglish() : employeeRecord.getNameBangla())
                    .email(officeUnit == null ? "" : officeUnit.getEmail())
                    .phoneNumber(officeUnit == null ? "" : officeUnit.getPhoneNumber())
                    .designation(designation)
                    .build();
        }).collect(Collectors.toList());
        return employeeRecordDTOS;
    }

    public OfficeUnitOrganogram getOfficeUnitOrganogramById(Long id) {
        return this.officeUnitOrganogramService.getOfficeUnitOrganogramById(id);
    }

    public ServiceOriginDTO getServiceDTOFromService(ServiceOrigin serviceOrigin) {
        return serviceOriginDAO.convertToServiceOriginDTO(serviceOrigin);
    }

    public CitizenCharterDTO getCitizenCharterDTOFromCitizenCharter(CitizenCharter citizenCharter) {
        return citizenCharterDAO.convertToCitizenCharterDTO(citizenCharter);
    }

    public boolean hasChildOffice(Long officeId) {
        return officeDAO.getChildCountByParentOfficeId(officeId) > 0;
    }

    public Page<ServiceOriginDTO> getAllServices(Pageable pageable) {
        return serviceOriginDAO.findAll(pageable);
    }

    public Page<CitizenCharterDTO> getAllCitizenChartersByOffice(Pageable pageable, Long officeId) {
        return citizenCharterDAO.findAll(pageable, officeId);
    }

    public Page<EmployeeRecordDTO> getAllEmployeeRecordByOffice(Pageable pageable, Office office) {
        List<EmployeeOffice> employeeOfficeList = employeeOfficeDAO.findByOfficeAndStatus(office, true);
        List<Long> employeeRecordIdList = new ArrayList();
        employeeOfficeList.stream().forEach(entry -> {
            if (entry.getEmployeeRecord() != null) {
                employeeRecordIdList.add(entry.getEmployeeRecord().getId());
            }
        });
        return employeeRecordDAO.findAllByIdInAsPageable(pageable, employeeRecordIdList);
    }

    public OfficesGRO getOfficesGRO(Long officeId) {
        return officesGroService.findOfficesGroByOfficeId(officeId);
    }

    public EmployeeRecordDTO getGRODetailsByOfficeId(Long officeId) {
        OfficesGRO officesGRO = getOfficesGRO(officeId);
        if (officesGRO == null || officesGRO.getGroOfficeUnitOrganogramId() == null) {
            return null;
        }
        List<EmployeeRecordDTO> employeeRecordDTOList = getListOfEmployeeRecordsFromGivenOfficeUnitOrganogram(officesGRO.getGroOfficeUnitOrganogramId());
        return employeeRecordDTOList.size() > 0 ? employeeRecordDTOList.get(0) : null;
    }

    public EmployeeRecordDTO getAODetailsByOfficeId(Long officeId) {
        OfficesGRO officesGRO = getOfficesGRO(officeId);
        if (officesGRO == null) {
            return null;
        }
        if (officesGRO.getAppealOfficerOfficeUnitOrganogramId() == null) {
            return null;
        }
        List<EmployeeRecordDTO> employeeRecordDTOList = getListOfEmployeeRecordsFromGivenOfficeUnitOrganogram(officesGRO.getAppealOfficerOfficeUnitOrganogramId());
        return employeeRecordDTOList.size() > 0 ? employeeRecordDTOList.get(0) : null;
    }

    public OfficesGroDTO getVisionMissionByOfficeId(Long officeId) {
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeId);
        return officesGroService.convertToOfficesGroDTO(officesGRO);
    }


    public OfficesGroDTO getOfficesVisionMission(Long officeId) {
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeId);
        if (officesGRO == null) {
            officesGRO = new OfficesGRO();
        }
        OfficesGroDTO officesGroDTO = officesGroService.convertToOfficesGroDTO(officesGRO);
        return officesGroDTO;
    }

    public CitizensCharterOriginDTO getOfficesVisionMission(Long layerLevel, Long officeOriginId) {
        return this.getOfficeOriginInfo(layerLevel, officeOriginId);
    }

    private CitizensCharterOriginDTO convertToCitizensCharterOriginDTO(CitizensCharterOrigin citizensCharterOrigin) {
        return CitizensCharterOriginDTO.builder()
                .expectationBangla(citizensCharterOrigin.getExpectationBangla())
                .expectationEnglish(citizensCharterOrigin.getExpectationEnglish())
                .missionBangla(citizensCharterOrigin.getMissionBangla())
                .missionEnglish(citizensCharterOrigin.getMissionEnglish())
                .visionBangla(citizensCharterOrigin.getVisionBangla())
                .visionEnglish(citizensCharterOrigin.getVisionEnglish())
                .build();
    }

    public OfficesGroDTO getOfficerCitizenCharter(Long officeId) {
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeId);
        if (officesGRO == null) {
            officesGRO = new OfficesGRO();
            Office office = getOffice(officeId);
            CustomOfficeLayer customOfficeLayer = office == null || office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : this.officeLayerDAO.getCustomOfficeLayerById(office.getOfficeLayer().getCustomLayerId().longValue());
            if (office != null) {
                officesGRO.setOfficeId(officeId);
                officesGRO.setOfficeOriginId(office.getOfficeOriginId());
                officesGRO.setOfficeNameBangla(office.getNameBangla());
                officesGRO.setOfficeNameEnglish(office.getNameEnglish());
            }

            officesGRO.setLayerLevel(office.getOfficeLayer() == null || office.getOfficeLayer().getLayerLevel() == null ? null : office.getOfficeLayer().getLayerLevel());
            officesGRO.setCustomLayerLevel(customOfficeLayer == null || customOfficeLayer.getLayerLevel() == null ? null : customOfficeLayer.getLayerLevel());
            officesGRO.setCustomLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getCustomLayerId() == null ? null : office.getOfficeLayer().getCustomLayerId());
            officesGRO.setOfficeLayerId(office.getOfficeLayer() == null || office.getOfficeLayer().getId() == null ? null : office.getOfficeLayer().getId());
            officesGRO.setOfficeOriginId(office.getOfficeOriginId() == null ? null : office.getOfficeOriginId());
            officesGRO.setOfficeMinistryId(office.getOfficeMinistry() == null || office.getOfficeMinistry().getId() == null ? null : office.getOfficeMinistry().getId());

        }
        if (officesGRO.getAdminOfficeId() == null) {
            OfficeUnitOrganogram officeUnitOrganogram = this.officeUnitOrganogramService.getAdminOrganogram(officeId);
            officesGRO.setAdminOfficeId(officeId);
            officesGRO.setAdminOfficeUnitOrganogramId(officeUnitOrganogram.getId());
            officesGRO.setAdminOfficeUnitName(officeUnitOrganogram.getOfficeUnit().getUnitNameBangla());
            officesGRO = officesGroService.save(officesGRO);
        }
        if (officesGRO.getAppealOfficeId() == null) {
            if (!isMinistryOrDivisionLevelOffice(officeId)) {
                Office office = officeDAO.findOne(officeId);
                Long appealOfficeId = office.getParentOfficeId();
                OfficesGRO appealOfficesGRO = officesGroService.findOfficesGroByOfficeId(appealOfficeId);
                if (appealOfficesGRO != null && appealOfficesGRO.getGroOfficeId() != null) {
                    officesGRO.setAppealOfficeId(appealOfficesGRO.getGroOfficeId());
                    officesGRO.setAppealOfficerOfficeUnitOrganogramId(appealOfficesGRO.getGroOfficeUnitOrganogramId());
                    officesGRO.setAoOfficeUnitName(appealOfficesGRO.getGroOfficeUnitName());
                    officesGRO = officesGroService.save(officesGRO);
                }
            }
        }
        return officesGroService.convertToOfficesGroDTO(officesGRO);
    }

    public Page<OfficesGroDTO> getOfficeSetUpMissing(Long missingOfficerType,
                                                     Long officeLayers,
                                                     Long firstSelection,
                                                     Long secondSelection,
                                                     Pageable pageable) {
        Set<Long> officeIdsinGroTable = officesGroService.findAllOffficeIdsIncludingInactive();
        List<OfficesGroDTO> officesGroDTOList = officesGroService.findOfficesGroByMissingOfficerType(
                missingOfficerType,
                officeLayers,
                firstSelection,
                secondSelection
        )
                .stream()
                .map(officesGRO -> DTOConverter.convertOfficesGROToDTO(officesGRO))
                .collect(Collectors.toList());
        officesGroDTOList.addAll(
                CacheUtil.getAllOfficeSearchDTOList()
                        .stream()
                        .filter(officeSearchDTO -> !officeIdsinGroTable.contains(officeSearchDTO.getId())
                                && missingOfficeMatchesLayers(
                                officeSearchDTO,
                                officeLayers,
                                firstSelection,
                                secondSelection
                        ))
                        .map(officeSearchDTO -> DTOConverter.convertOfficeSearchDTOtoOfficesGroDTO(officeSearchDTO))
                        .collect(Collectors.toList())
        );
        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), officesGroDTOList.size());
        return new PageImpl<>(officesGroDTOList.subList(start, end), pageable, officesGroDTOList.size());
    }

    public boolean missingOfficeMatchesLayers(
            OfficeSearchDTO officeSearchDTO,
            Long officeLayers,
            Long firstSelection,
            Long secondSelection
    ) {
        boolean matchedLayers = true;
        if (secondSelection != null) matchedLayers &= officeSearchDTO.getId().equals(secondSelection);
        else if (officeLayers != null) {

            if (officeLayers != 3) {
                matchedLayers &= Long.valueOf(officeSearchDTO.getLayerLevel().longValue()).equals(officeLayers);
            }
//            matchedLayers &= Long.valueOf(officeSearchDTO.getCustomLayerLevel().longValue()).equals(officeLayers);

            if (firstSelection != null) {

                if (officeLayers == 0 || officeLayers == 1 || officeLayers == 2) {

                } else if (officeLayers == 3) {
                    matchedLayers &= Long.valueOf(officeSearchDTO.getCustomLayerId().longValue()).equals(firstSelection);
                } else {
                    matchedLayers &= officeSearchDTO.getOriginId().equals(firstSelection);
                }

            }

        }

        return matchedLayers;
    }

    public RoleContainerDTO getOfficeUnitOrganogramsForLoggedInUser(UserInformation userInformation) {
        if (userInformation.getUserType().equals(UserType.COMPLAINANT) || (userInformation.getUserType().equals(UserType.SYSTEM_USER))) {
            return RoleContainerDTO.builder().build();
        }
        OfficeInformation officeInformation = userInformation.getOfficeInformation();
        RoleContainerDTO roleContainerDTO = RoleContainerDTO.builder().build();
        Long officeId = null;
        CellMember cellMember = null;
        if (officeInformation != null) {
            Long employeeRecordId = userInformation.getOfficeInformation().getEmployeeRecordId();
            officeId = userInformation.getOfficeInformation().getOfficeId();
            Long officeUnitOrganogramId = userInformation.getOfficeInformation().getOfficeUnitOrganogramId();
            roleContainerDTO = this.employeeOfficeDAO.findByEmployeeRecordId(officeId, employeeRecordId, officeUnitOrganogramId);
            List<Long> officeIds = roleContainerDTO.getRoles()
                    .stream()
                    .map(roleDTO -> roleDTO.getOfficeId())
                    .collect(Collectors.toList());

            List<Long> officeUnitOrganogramIds = roleContainerDTO.getRoles()
                    .stream()
                    .map(roleDTO -> roleDTO.getOfficeUnitOrganogramId())
                    .collect(Collectors.toList());

            cellMember = this.cellService.getCellMemberEntry(officeIds, officeUnitOrganogramIds);
        }
        if (cellMember != null) {
            String cellDesignation = "অভিযোগ ব্যবস্থাপনা সেল ";

            cellDesignation += cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য");

            roleContainerDTO.getRoles().add(
                    SingleRoleDTO.builder()
                            .officeUnitNameEnglish("Grievance Redress Cell")
                            .officeUnitNameBangla("অভিযোগ ব্যবস্থাপনা সেল সদস্য")
                            .selected(officeId == 0)
                            .officeUnitOrganogramId(cellMember.getCellOfficeUnitOrganogramId())
                            .designation(cellDesignation)
                            .officeId(0L)
                            .officeMinistryId(0L)
                            .officeNameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                            .officeNameEnglish("Cell")
                            .layerLevel(1L)
                            .geoDivisionId(0L)
                            .geoDistrictId(0L)
                            .officeUnitId(0L)
                            .build()
            );
        }
        return roleContainerDTO;
    }

    public Boolean isUpazilaLevelOffice(Long officeId) {
        return officeDAO.checkIfOfficeLayerIn(officeId, getOfficeLayersByLayerLevel(5));
    }

    public Boolean isZilaLevelOffice(Long officeId) {
        return officeDAO.checkIfOfficeLayerIn(officeId, getOfficeLayersByLayerLevel(5));
    }

    public Boolean hasChildOfficesOrIsDistrictLevelOffice(Long officeId) {
        return officeDAO.getChildCountByParentOfficeId(officeId) > 0 || isZilaLevelOffice(officeId);
    }

    public GroContactInfoResponseDTO getGROcontactInfoByOfficeId(Long officeId) {
        OfficesGRO assignedGROofAnOffice = this.officesGroService.findOfficesGroByOfficeId(officeId);
        Office officeOfGRO = this.getOffice(officeId);
        Long officeUnitOrganogramId = assignedGROofAnOffice.getGroOfficeUnitOrganogramId();
        OfficeUnitOrganogram officeUnitOrganogramOfGRO = this.officeUnitOrganogramService.getOfficeUnitOrganogramById(officeUnitOrganogramId);
        OfficeUnit officeUnitOfGRO = officeUnitOrganogramOfGRO.getOfficeUnit();
        EmployeeOffice employeeOffice = this.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officeId, officeUnitOrganogramId, true);

        if (officeId == 0) {
            CellMember cellMember = this.cellService.getCellMemberEntry(assignedGROofAnOffice.getGroOfficeUnitOrganogramId());
            officeOfGRO = this.getOffice(cellMember.getOfficeId());
            officeOfGRO.setNameBangla("অভিযোগ ব্যবস্থাপনা সেল");
            officeOfGRO.setNameEnglish("Cell");

            officeUnitOrganogramOfGRO = this.officeUnitOrganogramService.getOfficeUnitOrganogramById(cellMember.getOfficeUnitOrganogramId());
            officeUnitOfGRO = officeUnitOrganogramOfGRO.getOfficeUnit();

            String designationBn, designationEn;
            designationBn = cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য");
            designationEn = cellMember.getIsGro() ? "President" : (cellMember.getIsGro() ? "Member Secretary" : "Member");
            officeUnitOrganogramOfGRO.setDesignationBangla(designationBn);
            officeUnitOrganogramOfGRO.setDesignationEnglish(designationEn);
            officeUnitOfGRO.setUnitNameBangla("অভিযোগ ব্যবস্থাপনা সেল");
            officeUnitOfGRO.setUnitNameEnglish("Cell");
        }

        return GroContactInfoResponseDTO.builder()
                .nameBangla(employeeOffice.getEmployeeRecord().getNameBangla() == null ? Constant.NO_INFO_FOUND : employeeOffice.getEmployeeRecord().getNameBangla())
                .nameEnglish(employeeOffice.getEmployeeRecord().getNameBangla() == null ? Constant.NO_INFO_FOUND : employeeOffice.getEmployeeRecord().getNameEnglish())
                .officeNameBangla(officeOfGRO.getNameBangla() == null ? Constant.NO_INFO_FOUND : officeOfGRO.getNameBangla())
                .officeNameEnglish(officeOfGRO.getNameBangla() == null ? Constant.NO_INFO_FOUND : officeOfGRO.getNameEnglish())
                .designationBangla(officeUnitOrganogramOfGRO.getDesignationBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOrganogramOfGRO.getDesignationBangla())
                .designationEnglish(officeUnitOrganogramOfGRO.getDesignationBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOrganogramOfGRO.getDesignationEnglish())
                .officeUnitNameBangla(officeUnitOfGRO.getUnitNameBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOfGRO.getUnitNameBangla())
                .officeUnitNameEnglish(officeUnitOfGRO.getUnitNameBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOfGRO.getUnitNameEnglish())
                .email(officeUnitOfGRO.getEmail() == null ? Constant.NO_INFO_FOUND : officeUnitOfGRO.getEmail())
                .phoneNumber(officeUnitOfGRO.getPhoneNumber() == null ? Constant.NO_INFO_FOUND : officeUnitOfGRO.getPhoneNumber())
                .build();
    }

    public GroContactInfoResponseDTO getAoContactInfoByOfficeId(Long officeId) {
        OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(officeId);
        Office office = this.getOffice(officeId);
        Long officeUnitOrganogramId = officesGRO.getAppealOfficerOfficeUnitOrganogramId();
        OfficeUnitOrganogram officeUnitOrganogramOfAO = this.officeUnitOrganogramService.getOfficeUnitOrganogramById(officeUnitOrganogramId);
        OfficeUnit officeUnitOfAO = officeUnitOrganogramOfAO.getOfficeUnit();
        EmployeeOffice employeeOffice = this.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(officesGRO.getAppealOfficeId(), officeUnitOrganogramId, true);

        if (officeId == 0) {
            CellMember cellMember = this.cellService.getCellMemberEntry(officesGRO.getAppealOfficerOfficeUnitOrganogramId());
            office = this.getOffice(cellMember.getOfficeId());
            office.setNameBangla("অভিযোগ ব্যবস্থাপনা সেল");
            office.setNameEnglish("Cell");

            officeUnitOrganogramOfAO = this.officeUnitOrganogramService.getOfficeUnitOrganogramById(cellMember.getOfficeUnitOrganogramId());
            officeUnitOfAO = officeUnitOrganogramOfAO.getOfficeUnit();

            String designationBn, designationEn;
            designationBn = cellMember.getIsAo() ? "সভাপতি" : (cellMember.getIsGro() ? "সদস্য সচিব" : "সদস্য");
            designationEn = cellMember.getIsAo() ? "President" : "Member";
            officeUnitOrganogramOfAO.setDesignationBangla(designationBn);
            officeUnitOrganogramOfAO.setDesignationEnglish(designationEn);
            officeUnitOfAO.setUnitNameBangla("অভিযোগ ব্যবস্থাপনা সেল");
            officeUnitOfAO.setUnitNameEnglish("Cell");
        }

        return GroContactInfoResponseDTO.builder()
                .nameBangla(employeeOffice.getEmployeeRecord().getNameBangla() == null ? Constant.NO_INFO_FOUND : employeeOffice.getEmployeeRecord().getNameBangla())
                .nameEnglish(employeeOffice.getEmployeeRecord().getNameBangla() == null ? Constant.NO_INFO_FOUND : employeeOffice.getEmployeeRecord().getNameEnglish())
                .officeNameBangla(office.getNameBangla() == null ? Constant.NO_INFO_FOUND : office.getNameBangla())
                .officeNameEnglish(office.getNameBangla() == null ? Constant.NO_INFO_FOUND : office.getNameEnglish())
                .designationBangla(officeUnitOrganogramOfAO.getDesignationBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOrganogramOfAO.getDesignationBangla())
                .designationEnglish(officeUnitOrganogramOfAO.getDesignationBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOrganogramOfAO.getDesignationEnglish())
                .officeUnitNameBangla(officeUnitOfAO.getUnitNameBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOfAO.getUnitNameBangla())
                .officeUnitNameEnglish(officeUnitOfAO.getUnitNameBangla() == null ? Constant.NO_INFO_FOUND : officeUnitOfAO.getUnitNameEnglish())
                .email(officeUnitOfAO.getEmail() == null ? Constant.NO_INFO_FOUND : officeUnitOfAO.getEmail())
                .phoneNumber(officeUnitOfAO.getPhoneNumber() == null ? Constant.NO_INFO_FOUND : officeUnitOfAO.getPhoneNumber())
                .build();
    }

    public List<GrievanceCountByItemDTO> getListOfOfficeUnitsByOfficeId(Long officeId) {
        return officeUnitDAO.getListOfOfficeUnitsByOfficeId(officeId);
    }

    public List<GrievanceCountByItemDTO> getGrievanceCountByCitizensCharter(Long officeId) {
        return citizenCharterDAO.getGrievanceCountByServices(officeId);
    }

    public List<Office> getOfficeAlongWithAncestorOffices(UserInformation userInformation) {
        Long officeId = userInformation.getOfficeInformation().getOfficeId();
        Office office = this.getOffice(officeId);
        List<Office> offices = new ArrayList<Office>();
        if (!userInformation.getOisfUserType().equals(OISFUserType.GRO)) {
            offices.add(office);
        }
        while (office.getParentOffice() != null) {
            offices.add(office.getParentOffice());
            office = office.getParentOffice();
        }
        List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
        return offices.stream()
                .filter(officeObject -> officeIdsInOfficesGro.contains(officeObject.getId()))
                .collect(Collectors.toList());

    }

    public List<Long> getAncestorOfficeIds(Long officeId) {
        List<Long> officeIds = new ArrayList();
        Office office = this.getOffice(officeId);
        while (office.getParentOffice() != null) {
            office = office.getParentOffice();
            officeIds.add(office.getId());
        }
        List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
        return officeIds.stream()
                .filter(id -> officeIdsInOfficesGro.contains(id))
                .collect(Collectors.toList());

    }

    public OfficeUnit getOfficeUnitById(Long officeUnitId) {
        OfficeUnit officeUnit = this.officeUnitDAO.findOne(officeUnitId);
        return officeUnit;
    }

    public OfficeUnit getOfficeUnitByIdIncludingFakeOfficeUnitForCell(Long officeUnitId) {
        OfficeUnit officeUnit = this.officeUnitDAO.findOne(officeUnitId);
        if (officeUnitId == 0L) {
            officeUnit = OfficeUnit.builder()
                    .id(0L)
                    .unitNameBangla("অভিযোগ ব্যবস্থাপনা সেল")
                    .unitNameEnglish("Cell")
                    .build();
        }
        return officeUnit;
    }

    public OfficeLayerDTO convertToOfficeLayerDTO(OfficeLayer officeLayer) {
        return this.officeLayerDAO.convertToOfficeLayerDTO(officeLayer);
    }

    public CitizensCharterOriginDTO convertToOfficeOriginInfoDTO(CitizensCharterOrigin citizensCharterOrigin) {
        return this.officeLayerDAO.convertToOfficeOriginInfoDTO(citizensCharterOrigin);
    }

    public CitizensCharterOriginDTO getOfficeOriginInfo(Long layerLevel, Long officeOriginId) {
        return convertToOfficeOriginInfoDTO(this.officeOriginDAO.getOfficeOriginInfo(layerLevel, officeOriginId));
    }

    public CitizensCharterOriginDTO saveVisionMission(Long layerLevel, Long officeOriginId, KeyValueStringPairDTO keyValuePair) {
        CitizensCharterOrigin citizensCharterOrigin = officeOriginDAO.findByLayerLevelAndOfficeOriginId(layerLevel, officeOriginId);
        if (citizensCharterOrigin == null) {
            citizensCharterOrigin = CitizensCharterOrigin.builder()
                    .layerLevel(layerLevel)
                    .officeOriginId(officeOriginId)
                    .build();
        }
        try {
            Field field = citizensCharterOrigin.getClass().getDeclaredField(keyValuePair.getKey());
            field.setAccessible(true);
            ReflectionUtils.setField(field, citizensCharterOrigin, keyValuePair.getValue());
            citizensCharterOrigin = this.officeOriginDAO.save(citizensCharterOrigin);
            if (citizensCharterOrigin == null) {
                return null;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return convertToOfficeOriginInfoDTO(citizensCharterOrigin);
    }

    public EmployeeOffice findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId) {
        EmployeeOffice employeeOffice = this.employeeOfficeRepo.findByOfficeIdAndOfficeUnitOrganogramId(officeId, officeUnitOrganogramId);
        if (employeeOffice == null) {
            log.error("EmployeeOffice object is null from findEmployeeOfficeByOfficeIdAndOfficeUnitOrganogramId");
            return null;
        }
        return employeeOffice;
    }

    public ServiceOrigin getServiceOriginByServiceId(Long serviceId) {
        return this.serviceOriginDAO.findByServiceId(serviceId);
    }

    public EmployeeOffice findByOfficeUnitOrganogramIdAndStatus(Long id) {
        EmployeeOffice employeeOffice = this.employeeOfficeRepo.findByOfficeUnitOrganogramIdAndStatus(id);
        if (employeeOffice == null) {
            log.error("EmployeeOffice object is null from findByOfficeUnitOrganogramIdAndStatus");
            return null;
        }
        return employeeOffice;
    }

    public OfficeUnit getById(Long id) {
        return this.officeUnitDAO.findById(id);
    }


    public CitizenCharter convertFromServiceOrigin(ServiceOriginDTO serviceOriginDTO) {
        return CitizenCharter.builder()
                .officeOriginId(serviceOriginDTO.getOfficeOriginId())
                .originStatus(serviceOriginDTO.getStatus())
                .serviceTime(serviceOriginDTO.getServiceTime())
                .serviceNameBangla(serviceOriginDTO.getServiceNameBangla())
                .serviceNameEnglish(serviceOriginDTO.getServiceNameEnglish())
                .documentAndLocationBangla(serviceOriginDTO.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(serviceOriginDTO.getDocumentAndLocationEnglish())
                .paymentMethodBangla(serviceOriginDTO.getPaymentMethodBangla())
                .paymentMethodEnglish(serviceOriginDTO.getPaymentMethodEnglish())
                .serviceProcedureBangla(serviceOriginDTO.getServiceProcedureBangla())
                .serviceProcedureEnglish(serviceOriginDTO.getServiceProcedureEnglish())
                .status(serviceOriginDTO.getStatus())
                .serviceOrigin(this.serviceOriginDAO.convertToServiceOrigin(serviceOriginDTO))
                .build();
    }


    public List<CitizenCharterDTO> copyCitizenChartersFromOrigins(Long officeId, Long officeOriginId) {
        if (officeOriginId == null || officeOriginId == 0) {
            Office office = this.findOne(officeId);
            officeOriginId = office.getOfficeOriginId();
        }
        List<CitizenCharterDTO> citizenCharterDTOS = new ArrayList<>();
        List<ServiceOriginDTO> serviceOriginDTOS = this.serviceOriginDAO.findAllServicesByOfficeOrigin(officeOriginId);
        serviceOriginDTOS.forEach(serviceOriginDTO -> {
            Integer countService = this.citizenCharterDAO.countByServiceIdAndOfficeId(serviceOriginDTO.getId(), officeId);
            List<Long> tempSoOfficeId = new ArrayList<>();
            List<Long> tempSoUnitId = new ArrayList<>();
            List<Long> tempSoOrgId = new ArrayList<>();
            List<String> tempName = new ArrayList<>();
            List<String> tempDesignation = new ArrayList<>();
            List<String> tempMobile = new ArrayList<>();
            List<String> tempOfficeName = new ArrayList<>();
            List<String> tempOfficeUnit = new ArrayList<>();
            CitizenCharterDTO citizenCharterDTO;
            if (countService.equals(0)) {
                CitizenCharter citizenCharter = convertFromServiceOrigin(serviceOriginDTO);
                citizenCharter.setOfficeId(officeId);
                List<OfficeUnitOrganogram> officeUnitOrganograms = this.officeOrganogramService.findOfficeUnitOrganogramByOfficeOriginUnitOrgIdAndOfficeId(serviceOriginDTO.getOfficeOriginUnitOrganogramId(), officeId);
                if (officeUnitOrganograms.size() == 0) {
                    citizenCharterDAO.saveCitizenCharter(citizenCharter);
                    citizenCharterDTO = convertToCitizenCharterDTO(citizenCharter);
                    tempName.add("এই পদে কর্মকর্তা নেই");
                    tempMobile.add("-");
                    tempDesignation.add("-");
                    tempOfficeUnit.add("-");
                    tempOfficeName.add("-");
                    citizenCharterDTO.setNames(tempName);
                    citizenCharterDTO.setPhoneNumbers(tempMobile);
                    citizenCharterDTO.setDesignations(tempDesignation);
                    citizenCharterDTO.setOfficeNames(tempOfficeName);
                    citizenCharterDTO.setOfficeUnits(tempOfficeUnit);
                } else {
                    citizenCharterDTO = convertToCitizenCharterDTO(citizenCharter);
                    officeUnitOrganograms.forEach(officeUnitOrganogram -> {
                        OfficeUnit officeUnit = officeUnitOrganogram.getOfficeUnit();
                        EmployeeOffice employeeOffice = officeUnitOrganogram == null ? null : employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeUnitOrganogram.getOfficeId(), officeUnitOrganogram.getId());
                        if (officeUnitOrganograms.size() == 1) {
                            citizenCharter.setSoOfficeId(officeId);
                            citizenCharter.setSoOfficeUnitId(officeUnit == null ? null : officeUnit.getId());
                            citizenCharter.setSoOfficeUnitOrganogramId(officeUnitOrganogram.getId());
                            citizenCharterDAO.saveCitizenCharter(citizenCharter);
                        } else {
                            citizenCharterDAO.saveCitizenCharter(citizenCharter);
                        }
                        tempSoOfficeId.add(officeId);
                        tempSoUnitId.add(officeUnit == null ? null : officeUnit.getId());
                        tempSoOrgId.add(officeUnitOrganogram.getId());
                        citizenCharterDTO.setSoOfficeIds(tempSoOfficeId);
                        citizenCharterDTO.setSoOfficeUnitIds(tempSoUnitId);
                        citizenCharterDTO.setSoOfficeUnitOrganogramIds(tempSoOrgId);
                        EmployeeRecord employeeRecord = null;
                        if (employeeOffice != null && officeUnit != null) {
                            employeeRecord = this.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                            tempName.add(employeeRecord.getNameBangla());
                            tempDesignation.add(officeUnitOrganogram.getDesignationBangla());
                            tempMobile.add(employeeRecord.getPersonalMobile());
                            tempOfficeName.add(getOfficeName(officeId));
                            tempOfficeUnit.add(officeUnit.getUnitNameBangla());
                        } else {
                            tempName.add("এই পদে কর্মকর্তা নেই");
                            tempMobile.add("-");
                            tempDesignation.add("-");
                            tempOfficeUnit.add("-");
                            tempOfficeName.add("-");
                        }
                        citizenCharterDTO.setNames(tempName);
                        citizenCharterDTO.setPhoneNumbers(tempMobile);
                        citizenCharterDTO.setDesignations(tempDesignation);
                        citizenCharterDTO.setOfficeNames(tempOfficeName);
                        citizenCharterDTO.setOfficeUnits(tempOfficeUnit);
                    });
                }
            } else {
                citizenCharterDTO = convertToCitizenCharterDTO(this.citizenCharterDAO.findByServiceIdAndOfficeId(serviceOriginDTO.getId(), officeId));
                Long soOfficeUnitId = citizenCharterDTO == null ? null : citizenCharterDTO.getSoOfficeUnitId();
                if (soOfficeUnitId != null) {
                    OfficeUnit officeUnit = this.officeUnitDAO.findById(citizenCharterDTO.getSoOfficeUnitId());
                    OfficeUnitOrganogram officeUnitOrganogram = this.officeOrganogramService.findOfficeUnitOrganogramById(citizenCharterDTO.getSoOfficeUnitOrganogramId());
                    EmployeeOffice employeeOffice = officeUnitOrganogram == null ? null : employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeUnitOrganogram.getOfficeId(), officeUnitOrganogram.getId());
                    EmployeeRecord employeeRecord = null;
                    if (employeeOffice != null && officeUnit != null && employeeOffice.getEmployeeRecord() != null) {
                        employeeRecord = this.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                        tempName.add(employeeRecord.getNameBangla());
                        tempDesignation.add(officeUnitOrganogram.getDesignationBangla());
                        tempMobile.add(employeeRecord.getPersonalMobile());
                        tempOfficeName.add(getOfficeName(officeId));
                        tempOfficeUnit.add(officeUnit.getUnitNameBangla());
                        tempSoOfficeId.add(officeId);
                        tempSoUnitId.add(officeUnit.getId());
                        tempSoOrgId.add(officeUnitOrganogram.getId());
                    } else {
                        tempName.add("এই পদে কর্মকর্তা নেই");
                        tempMobile.add("-");
                        tempDesignation.add("-");
                        tempOfficeUnit.add("-");
                        tempOfficeName.add("-");
                    }
                    citizenCharterDTO.setNames(tempName);
                    citizenCharterDTO.setPhoneNumbers(tempMobile);
                    citizenCharterDTO.setDesignations(tempDesignation);
                    citizenCharterDTO.setOfficeNames(tempOfficeName);
                    citizenCharterDTO.setOfficeUnits(tempOfficeUnit);
                    citizenCharterDTO.setSoOfficeIds(tempSoOfficeId);
                    citizenCharterDTO.setSoOfficeUnitIds(tempSoUnitId);
                    citizenCharterDTO.setSoOfficeUnitOrganogramIds(tempSoOrgId);
                    citizenCharterDTO.setStatus(citizenCharterDTO.getStatus());
                } else {
                    List<OfficeUnitOrganogram> officeUnitOrganograms = this.officeOrganogramService.findOfficeUnitOrganogramByOfficeOriginUnitOrgIdAndOfficeId(serviceOriginDTO.getOfficeOriginUnitOrganogramId(), officeId);
                    if (officeUnitOrganograms.size() == 0) {
                        tempName.add("এই পদে কর্মকর্তা নেই");
                        tempMobile.add("-");
                        tempDesignation.add("-");
                        tempOfficeUnit.add("-");
                        tempOfficeName.add("-");
                        citizenCharterDTO.setNames(tempName);
                        citizenCharterDTO.setPhoneNumbers(tempMobile);
                        citizenCharterDTO.setDesignations(tempDesignation);
                        citizenCharterDTO.setOfficeNames(tempOfficeName);
                        citizenCharterDTO.setOfficeUnits(tempOfficeUnit);
                    } else {
                        officeUnitOrganograms.forEach(officeUnitOrganogram -> {
                            OfficeUnit officeUnit = officeUnitOrganogram.getOfficeUnit();
                            EmployeeOffice employeeOffice = officeUnitOrganogram == null ? null : employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeUnitOrganogram.getOfficeId(), officeUnitOrganogram.getId());
                            tempSoOfficeId.add(officeId);
                            tempSoOrgId.add(officeUnitOrganogram.getId());
                            citizenCharterDTO.setSoOfficeIds(tempSoOfficeId);
                            citizenCharterDTO.setSoOfficeUnitIds(tempSoUnitId);
                            citizenCharterDTO.setSoOfficeUnitOrganogramIds(tempSoOrgId);
                            EmployeeRecord employeeRecord = null;
                            tempSoUnitId.add(officeUnit == null ? null : officeUnit.getId());
                            if (employeeOffice != null && officeUnit != null) {
                                employeeRecord = this.findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
                                tempName.add(employeeRecord.getNameBangla());
                                tempDesignation.add(officeUnitOrganogram.getDesignationBangla());
                                tempMobile.add(employeeRecord.getPersonalMobile());
                                tempOfficeName.add(getOfficeName(officeId));
                                tempOfficeUnit.add(officeUnit.getUnitNameBangla());
                            } else {
                                tempName.add("এই পদে কর্মকর্তা নেই");
                                tempMobile.add("-");
                                tempDesignation.add("-");
                                tempOfficeUnit.add("-");
                                tempOfficeName.add("-");
                            }
                            citizenCharterDTO.setNames(tempName);
                            citizenCharterDTO.setPhoneNumbers(tempMobile);
                            citizenCharterDTO.setDesignations(tempDesignation);
                            citizenCharterDTO.setOfficeNames(tempOfficeName);
                            citizenCharterDTO.setOfficeUnits(tempOfficeUnit);
                        });
                    }
                }
            }
            citizenCharterDTOS.add(citizenCharterDTO);
        });
        List<CitizenCharterDTO> finalCC = new ArrayList<>();
        citizenCharterDTOS.forEach(citizenCharterDTO -> {
            if (citizenCharterDTO.getOriginStatus() != null && citizenCharterDTO.getOriginStatus()) {
                finalCC.add(citizenCharterDTO);
            }
        });
        return finalCC;
    }

    public CitizenCharterDTO convertToCitizenCharterDTO(CitizenCharter citizenCharter) {
        return citizenCharter == null ? CitizenCharterDTO.builder().build() : CitizenCharterDTO.builder()
                .id(citizenCharter.getId())
                .officeId(citizenCharter.getOfficeId())
                .officeOriginId(citizenCharter.getOfficeOriginId())
                .soOfficeId(citizenCharter.getSoOfficeId())
                .soOfficeUnitId(citizenCharter.getSoOfficeUnitId())
                .soOfficeUnitOrganogramId(citizenCharter.getSoOfficeUnitOrganogramId())
                .serviceId(citizenCharter.getServiceOrigin().getId())
                .serviceNameBangla(citizenCharter.getServiceNameBangla())
                .serviceNameEnglish(citizenCharter.getServiceNameEnglish())
                .serviceProcedureBangla(citizenCharter.getServiceProcedureBangla())
                .serviceProcedureEnglish(citizenCharter.getServiceProcedureEnglish())
                .paymentMethodBangla(citizenCharter.getPaymentMethodBangla())
                .paymentMethodEnglish(citizenCharter.getPaymentMethodEnglish())
                .documentAndLocationBangla(citizenCharter.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(citizenCharter.getDocumentAndLocationEnglish())
                .serviceTime(citizenCharter.getServiceTime())
                .status(citizenCharter.getStatus())
                .originStatus(citizenCharter.getOriginStatus())
                .build();
    }

    public CitizensCharterOriginWithServiceOriginsDTO getCitizensCharterOriginWithServiceOriginList(Long layerLevel, Long officeOriginId) {
        return CitizensCharterOriginWithServiceOriginsDTO.builder()
                .citizensCharterOrigin(getOfficeOriginInfo(layerLevel, officeOriginId))
                .serviceOriginList(serviceOriginDAO.findAllServiceOriginDTO(officeOriginId))
                .build();
    }

    public ServiceOriginDTO getServiceOriginDTObyId(Long id) {
        ServiceOrigin serviceOrigin = serviceOriginDAO.findById(id);

        if (serviceOrigin == null) {
            serviceOrigin = ServiceOrigin.builder().build();
        }
        return serviceOriginDAO.convertToServiceOriginDTO(serviceOrigin);
    }

    public List<OfficeOriginUnitDTO> getOfficeOriginUnitDTOListByOfficeOriginId(Long officeOriginId) {
        return officeOriginDAO.getOfficeOriginUnitDTOListByOfficeOriginId(officeOriginId);
    }

    public List<OfficeOriginUnitOrganogramDTO> getOfficeOriginUnitOrganogramDTOListByOfficeOriginUnitId(Long officeOriginUnitId) {
        return officeOriginDAO.getOfficeOriginUnitOrganogramDTOListByOfficeOriginUnitId(officeOriginUnitId);
    }

    public List<Office> findByIdContainsInList(List<Long> idList) {
        return officeDAO.findByIdContainsInList(idList);
    }

    public GroAoEmployeeRecordsDTO getGroAndAoEmployeeRecords(Long officeId) {
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeId);
        EmployeeOffice groEmployeeOffice = employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officesGRO.getGroOfficeId(), officesGRO.getGroOfficeUnitOrganogramId());
        EmployeeOffice aoEmployeeOffice = employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officesGRO.getAppealOfficeId(), officesGRO.getAppealOfficerOfficeUnitOrganogramId());
        EmployeeRecordDTO groEmployeeRecordDTO = null;
        EmployeeRecordDTO aoEmployeeRecordDTO = null;
        if (groEmployeeOffice != null) {
            groEmployeeRecordDTO = employeeRecordDAO.getEmployeeRecordDTObyEmployeeRecordId(groEmployeeOffice.getEmployeeRecord().getId());
        }
        if (aoEmployeeOffice != null) {
            aoEmployeeRecordDTO = employeeRecordDAO.getEmployeeRecordDTObyEmployeeRecordId(aoEmployeeOffice.getEmployeeRecord().getId());
        }
        return GroAoEmployeeRecordsDTO.builder()
                .groRecord(groEmployeeRecordDTO)
                .aoRecord(aoEmployeeRecordDTO)
                .build();
    }

    public CitizenCharterDTO assignSO(CitizenCharterDTO citizenCharterDTO, Long officeId, Long serviceId) {
        CitizenCharter citizenCharter = this.citizenCharterDAO.findByServiceIdAndOfficeId(serviceId, officeId);
        OfficeUnitOrganogram officeUnitOrganogram = this.officeOrganogramService.findOfficeUnitOrganogramById(citizenCharterDTO.getSoOfficeUnitOrganogramId());
        OfficeUnit officeUnit = officeUnitOrganogram.getOfficeUnit();
        if (officeUnit == null) {
            CitizenCharterDTO existingCitizenCharterDTO = convertToCitizenCharterDTO(citizenCharter);
            existingCitizenCharterDTO.setName("এই শাখা বর্তমানে খালি আছে");
            existingCitizenCharterDTO.setOfficeUnitName("-");
            existingCitizenCharterDTO.setOfficeName("-");
            existingCitizenCharterDTO.setDesignation("-");
            existingCitizenCharterDTO.setMobileNumber("-");
            return existingCitizenCharterDTO;
        }
        citizenCharter.setSoOfficeId(citizenCharterDTO.getSoOfficeId());
        citizenCharter.setSoOfficeUnitId(officeUnit == null ? null : officeUnit.getId());
        citizenCharter.setSoOfficeUnitOrganogramId(citizenCharterDTO.getSoOfficeUnitOrganogramId());

        CitizenCharterDTO updatedCitizenCharterDTO = convertToCitizenCharterDTO(this.citizenCharterDAO.saveCitizenCharter(citizenCharter));

        EmployeeRecord employeeRecord = null;
//        EmployeeOffice employeeOffice = findByOfficeUnitOrganogramIdAndStatus(officeUnitOrganogram.getId());
        EmployeeOffice employeeOffice = officeUnitOrganogram == null ? null : employeeOfficeDAO.findByOfficeIdAndOfficeUnitOrganogramId(officeUnitOrganogram.getOfficeId(), officeUnitOrganogram.getId());
        if (employeeOffice != null) {
            employeeRecord = findEmployeeRecordById(employeeOffice.getEmployeeRecord().getId());
            updatedCitizenCharterDTO.setName(employeeRecord.getNameBangla());
            updatedCitizenCharterDTO.setMobileNumber(employeeRecord.getPersonalMobile());
            updatedCitizenCharterDTO.setDesignation(officeUnitOrganogram.getDesignationBangla());
            updatedCitizenCharterDTO.setOfficeUnitName(officeUnit == null ? "" : officeUnit.getUnitNameBangla());
            updatedCitizenCharterDTO.setOfficeName(getOfficeName(officeId));
        } else {
            updatedCitizenCharterDTO.setName("এই পদের কর্মকর্তার তথ্য ত্রুটিযুক্ত");
            updatedCitizenCharterDTO.setMobileNumber("-");
            updatedCitizenCharterDTO.setOfficeName("-");
            updatedCitizenCharterDTO.setDesignation("-");
            updatedCitizenCharterDTO.setOfficeUnitName("-");
        }
        return updatedCitizenCharterDTO;
    }


    public List<OfficeOrigin> getDistinctOfficeOrigins(Integer layerLevel, Boolean grsEnabled) {
        List<OfficeLayer> officeLayerList = getOfficeLayersByLayerLevel(layerLevel);
        List<Long> officeLayerIds = officeLayerList.stream()
                .map(officeLayer -> officeLayer.getId())
                .collect(Collectors.toList());
        List<OfficeOrigin> officeOrigins = this.officeOriginDAO.findDistinctOfficeOrigins(officeLayerIds);
        if (grsEnabled) {
            List<Long> officeOriginIds = this.officesGroService.findAllOffficeOriginIds();
            return officeOrigins.stream()
                    .filter(officeOrigin -> officeOriginIds.contains(officeOrigin.getId()))
                    .collect(Collectors.toList());
        } else {
            return officeOrigins;
        }

    }

    public List<OfficeLayer> getOfficeLayersUsingCustomLayer(Integer layerLevel) {
        List<CustomOfficeLayer> customOfficeLayerList = officeLayerDAO.getCustomOfficeLayersByLayerLevel(layerLevel);
        List<Integer> customOfficeLayerIds = customOfficeLayerList.stream()
                .map(customOfficeLayer -> customOfficeLayer.getId().intValue())
                .collect(Collectors.toList());
        return getOfficeLayersByLayerLevelAndCustomLayerIdInList(layerLevel, customOfficeLayerIds);
    }

    public List<OfficeOrigin> getOfficeOriginsByLayerLevel(Integer layerLevel, Boolean grsEnabled) {
        return getOfficeOriginsByLayerLevel(layerLevel, grsEnabled, false);
    }

    public List<OfficeOrigin> getOfficeOriginsByLayerLevel(Integer layerLevel, Boolean grsEnabled, Boolean showChildOfficesOnly) {
        List<OfficeLayer> officeLayerList = getOfficeLayersUsingCustomLayer(layerLevel);
        List<Long> officeLayerIds = officeLayerList.stream()
                .map(OfficeLayer::getId)
                .collect(Collectors.toList());
        List<OfficeOrigin> officeOrigins = this.officeOriginDAO.findDistinctOfficeOrigins(officeLayerIds);
        if (grsEnabled) {
            List<Long> officeOriginIds = this.officesGroService.findAllOffficeOriginIds();
            officeOrigins = officeOrigins.stream()
                    .filter(officeOrigin -> officeOriginIds.contains(officeOrigin.getId()))
                    .collect(Collectors.toList());
        }
        UserInformation userInformation = getCurrentLoggedInUserInformationObject();

        if (userInformation != null && userInformation.getOfficeInformation() !=null && userInformation.getOfficeInformation().getOfficeId() != 28 && showChildOfficesOnly && (userInformation.getIsCentralDashboardUser() == null || !userInformation.getIsCentralDashboardUser())) {
            List<Long> childOfficeOriginIds = getListOfDescendantOfficeOriginIds();
            officeOrigins = officeOrigins.stream()
                    .filter(origin -> childOfficeOriginIds.contains(origin.getId()))
                    .collect(Collectors.toList());
        }
        return officeOrigins;
    }

    public List<Office> getOfficesByLayerLevelAndCustomLayerId(Integer layerLevel, Integer customLayerId, Boolean grsEnabled) {
        return getOfficesByLayerLevelAndCustomLayerId(layerLevel, customLayerId, grsEnabled, false);
    }

    public List<Office> getOfficesByLayerLevelAndCustomLayerId(Integer layerLevel, Integer customLayerId, Boolean grsEnabled, Boolean showChildOfficesOnly) {
        List<OfficeLayer> officeLayerList = getOfficeLayersByLayerLevelAndCustomLayerId(layerLevel, customLayerId);
        List<Office> offices = getOfficesByOfficeLayer(officeLayerList, grsEnabled);
        UserInformation userInformation = getCurrentLoggedInUserInformationObject();

        if (showChildOfficesOnly && (userInformation.getIsCentralDashboardUser() == null || !userInformation.getIsCentralDashboardUser())) {
            List<Long> childOfficesIds = getListOfDescendantOfficeIds();
            offices = offices.stream()
                    .filter(office -> childOfficesIds.contains(office.getId()))
                    .collect(Collectors.toList());
        }
        return offices;
    }

    public List<Office> findByOfficeOriginId(Long officeoriginId, Boolean grsEnabled) {
        return findByOfficeOriginId(officeoriginId, grsEnabled, false);
    }

    public List<Office> findByOfficeOriginId(Long officeoriginId, Boolean grsEnabled, Boolean showChildOfficesOnly) {
        List<Office> offices = this.officeDAO.findByOfficeOriginId(officeoriginId);
        if (grsEnabled) {
            List<Long> officeIds = this.officesGroService.findAllOffficeIds();
            offices = offices.stream()
                    .filter(officeOrigin -> officeIds.contains(officeOrigin.getId()))
                    .collect(Collectors.toList());
        }
        UserInformation userInformation = getCurrentLoggedInUserInformationObject();

        if (userInformation != null && userInformation.getOfficeInformation()!=null && userInformation.getOfficeInformation().getOfficeId() != 28 && showChildOfficesOnly && (userInformation.getIsCentralDashboardUser() == null || !userInformation.getIsCentralDashboardUser())) {
            List<Long> childOfficesIds = getListOfDescendantOfficeIds();
            offices = offices.stream()
                    .filter(office -> childOfficesIds.contains(office.getId()))
                    .collect(Collectors.toList());
        }
        return offices;
    }

    public List<Office> findByOfficeOriginIds(List<Long> officeoriginIds, Boolean grsEnabled, Boolean showChildOfficesOnly) {
        List<Office> offices = this.officeDAO.findByOfficeOriginIds(officeoriginIds);
        if (grsEnabled) {
            List<Long> officeIds = this.officesGroService.findAllOffficeIds();
            offices = offices.stream()
                    .filter(officeOrigin -> officeIds.contains(officeOrigin.getId()))
                    .collect(Collectors.toList());
        }
        UserInformation userInformation = getCurrentLoggedInUserInformationObject();

        if (userInformation != null && userInformation.getOfficeInformation()!= null && userInformation.getOfficeInformation().getOfficeId() != 28 && showChildOfficesOnly && (userInformation.getIsCentralDashboardUser() == null || !userInformation.getIsCentralDashboardUser())) {
            List<Long> childOfficesIds = getListOfDescendantOfficeIds();
            offices = offices.stream()
                    .filter(office -> childOfficesIds.contains(office.getId()))
                    .collect(Collectors.toList());
        }
        return offices;
    }

    public List<Office> findGRSenabledOfficesByOfficeOriginId(Long officeOriginId) {
        return this.findByOfficeOriginId(officeOriginId, true);
    }

    public UserInformation switchOISFUserRole(UserInformation userInformation, SingleRoleDTO roleDTO) {
        UserInformation prevInformation = userInformation;
        OfficeInformation officeInformation = OfficeInformation.builder()
                .officeId(roleDTO.getOfficeId())
                .officeOriginId(roleDTO.getOfficeOriginId())
                .designation(roleDTO.getDesignation())
                .officeUnitOrganogramId(roleDTO.getOfficeUnitOrganogramId())
                .officeMinistryId(roleDTO.getOfficeMinistryId())
                .officeNameBangla(roleDTO.getOfficeNameBangla())
                .officeNameEnglish(roleDTO.getOfficeNameEnglish())
                .employeeRecordId(userInformation.getOfficeInformation().getEmployeeRecordId())
                .geoDistrictId(roleDTO.getGeoDistrictId())
                .geoDivisionId(roleDTO.getGeoDivisionId())
                .layerLevel(roleDTO.getLayerLevel())
                .name(userInformation.getOfficeInformation().getName())
                .build();

        userInformation.setOfficeInformation(officeInformation);
        OfficeUnitOrganogram officeUnitOrganogram = officeOrganogramService.findOfficeUnitOrganogramById(officeInformation.getOfficeUnitOrganogramId());
        EmployeeRecord employeeRecord = this.findEmployeeRecordById(officeInformation.getEmployeeRecordId());
        OfficesGRO officesGRO = officesGroService.findOfficesGroByOfficeId(officeInformation.getOfficeId());

        List<EmployeeOffice> employeeOffices = employeeRecord.getEmployeeOffices()
                .stream()
                .filter(EmployeeOffice::getIsOfficeHead)
                .collect(Collectors.toList());

        if (officesGRO != null && officesGRO.getGroOfficeUnitOrganogramId() != null && officesGRO.getGroOfficeUnitOrganogramId().equals(officeInformation.getOfficeUnitOrganogramId())) {
            userInformation.setOisfUserType(OISFUserType.GRO);
        } else if (employeeOffices.size() > 0 && employeeOffices.get(0).getOfficeUnitOrganogram().equals(officeUnitOrganogram)) {
            userInformation.setOisfUserType(OISFUserType.HEAD_OF_OFFICE);
        } else {
            userInformation.setOisfUserType(OISFUserType.SERVICE_OFFICER);
        }
        List<OfficesGRO> officesGROsAsAppealOfficer = new ArrayList<>();
        List<OfficesGRO> officesGROsAsOfficeAdmin = new ArrayList<>();
        Long organogramId = officeInformation.getOfficeUnitOrganogramId();
        boolean hasCentralDashboardAccess = false, isCellGRO = false;
        if (organogramId != null) {
            officesGROsAsAppealOfficer = this.officesGroService.findByAppealOfficeUnitOrganogramId(organogramId);
            officesGROsAsOfficeAdmin = this.officesGroService.findByAdminOfficeUnitOrganogramId(organogramId);
            hasCentralDashboardAccess = centralDashboardRecipientDAO.hasAccessToCentralDashboard(officeInformation.getOfficeId(), organogramId);
            CellMember cellMember = cellService.getCellMemberEntry(officeInformation.getOfficeUnitOrganogramId());
            if (cellMember != null) {
                isCellGRO = cellService.isCellGRO(officeInformation) || cellMember.getIsGro();
            } else {
                isCellGRO = cellService.isCellGRO(officeInformation);
            }

        }
        userInformation.setIsCellGRO(isCellGRO);
        userInformation.setIsAppealOfficer(officesGROsAsAppealOfficer.size() > 0 || isCellGRO);
        userInformation.setIsOfficeAdmin(officesGROsAsOfficeAdmin.size() > 0);
        userInformation.setIsCentralDashboardUser(hasCentralDashboardAccess);
        return userInformation;
    }

    public Boolean hasAccessToAoAndSubOfficesDashboard(UserInformation userInformation, Long officeId) {
        Office office = officeDAO.findOne(officeId);
        Boolean aboveDistrictLevelOffice = (office.getOfficeLayer() != null && office.getOfficeLayer().getLayerLevel() < Constant.districtLayerLevel);
        Boolean hasChildOffice = hasChildOffice(officeId);
        Boolean validUser = (userInformation.getIsAppealOfficer() || userInformation.getOisfUserType().equals(OISFUserType.HEAD_OF_OFFICE) || userInformation.getIsCentralDashboardUser());
        return validUser && hasChildOffice && aboveDistrictLevelOffice;
    }

    public Boolean canViewDashboardAsFieldCoordinator(Authentication authentication, Long officeId) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        OfficeInformation officeInformation = userInformation.getOfficeInformation();
        Office targetOffice = officeDAO.findOne(officeId);
        Office thisOffice = officeDAO.findOne(officeInformation.getOfficeId());
        if (Utility.isDivisionLevelFC(authentication)) {
            if (thisOffice.getDivisionId() == targetOffice.getDivisionId()) {
                return true;
            }
        } else if (Utility.isDistrictLevelFC(authentication)) {
            if (thisOffice.getDistrictId() == targetOffice.getDistrictId()) {
                return true;
            }
        }
        return false;
    }

    public List<CellMemberInfoDTO> getCellMembersInfo(List<CellMember> cellMembers) {
        List<CellMemberInfoDTO> cellMemberInfoDTOS = cellMembers.stream()
                .map(cellMember -> {
                    Long officeId = cellMember.getOfficeId();
                    Long officeUnitOrganogramId = cellMember.getOfficeUnitOrganogramId();
                    Long employeeRecordId = cellMember.getEmployeeRecordId();

                    SingleRoleDTO role = this.findSingleRole(officeId, officeUnitOrganogramId);
                    EmployeeRecord employeeRecord = this.findEmployeeRecordById(employeeRecordId);
                    Boolean isNull = role == null;

                    return CellMemberInfoDTO.builder()
                            .id(cellMember.getId())
                            .nameBangla(employeeRecord.getNameBangla())
                            .nameEnglish(employeeRecord.getNameEnglish())
                            .officeUnitOrganogramId(isNull ? null : role.getOfficeUnitOrganogramId())
                            .designation(isNull ? null : role.getDesignation())
                            .officeId(isNull ? null : role.getOfficeId())
                            .officeNameBangla(isNull ? "" : role.getOfficeNameBangla())
                            .officeNameEnglish(isNull ? "" : role.getOfficeNameEnglish())
                            .officeUnitId(isNull ? null : role.getOfficeUnitId())
                            .officeUnitNameBangla(isNull ? "" : role.getOfficeUnitNameBangla())
                            .officeUnitNameEnglish(isNull ? "" : role.getOfficeUnitNameEnglish())
                            .isGro(cellMember.getIsGro())
                            .isAppealOfficer(cellMember.getIsAo())
                            .build();
                }).collect(Collectors.toList());
        return cellMemberInfoDTOS;
    }

    public boolean checkIfUserOfficeExistsInAncestorsList(Long officeId) {
        List<Long> descendantOfficeIds = getListOfDescendantOfficeIds();
        return descendantOfficeIds.contains(officeId);
    }

    public List<Office> getGrsEnabledDivisionOffices(Long divisionId) {
        List<OfficeLayer> officeLayers = getOfficeLayersByLayerLevel(Constant.divisionLayerLevel.intValue());
        List<Office> offices = officeDAO.getOfficesByDivisionIdAndOfficeLayers(divisionId, officeLayers);
        List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
        return offices.stream()
                .filter(office -> officeIdsInOfficesGro.contains(office.getId()))
                .collect(Collectors.toList());
    }

    public List<Office> getDivisionLevelOffices(Integer divisionId) {
        List<CustomOfficeLayer> customOfficeLayerList = officeLayerDAO.getCustomOfficeLayersByLayerLevel(Constant.divisionLayerLevel.intValue());
        List<OfficeLayer> officeLayers = getOfficeLayersByLayerLevelAndCustomLayerIdInList(Constant.divisionLayerLevel.intValue(), customOfficeLayerList.stream()
                .map(customOfficeLayer -> customOfficeLayer.getId().intValue()).collect(Collectors.toList()));
        return officeDAO.getOfficesByDivisionIdAndOfficeLayers(Long.valueOf(divisionId), officeLayers);
    }

    public List<Office> getDistrictLevelOffices(Integer divisionId, Integer districtId) {
        List<CustomOfficeLayer> customOfficeLayerList = officeLayerDAO.getCustomOfficeLayersByLayerLevel(Constant.divisionLayerLevel.intValue());
        List<OfficeLayer> officeLayers = getOfficeLayersByLayerLevelAndCustomLayerIdInList(Constant.divisionLayerLevel.intValue(), customOfficeLayerList.stream()
                .map(customOfficeLayer -> customOfficeLayer.getId().intValue()).collect(Collectors.toList()));
        return officeDAO.getOfficesByDivisionIdAndDistrictIdAndOfficeLayers(divisionId, districtId, officeLayers);
    }

    public List<Office> getUpazilaLevelOffices(Integer divisionId, Integer districtId, Integer upazilaId) {
        List<CustomOfficeLayer> customOfficeLayerList = officeLayerDAO.getCustomOfficeLayersByLayerLevel(Constant.divisionLayerLevel.intValue());
        List<OfficeLayer> officeLayers = getOfficeLayersByLayerLevelAndCustomLayerIdInList(Constant.divisionLayerLevel.intValue(), customOfficeLayerList.stream()
                .map(customOfficeLayer -> customOfficeLayer.getId().intValue()).collect(Collectors.toList()));
        return officeDAO.getOfficesByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeLayers(divisionId, districtId, upazilaId, officeLayers);
    }

    public List<Office> getGrsEnabledDistrictOffices(Long districtId) {
        List<OfficeLayer> officeLayers = getOfficeLayersByLayerLevel(Constant.layerFour.intValue());
        List<Office> offices = officeDAO.getOfficesByDistrictIdAndOfficeLayers(districtId, officeLayers);
        List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
        return offices.stream()
                .filter(office -> officeIdsInOfficesGro.contains(office.getId()))
                .collect(Collectors.toList());
    }

    public List<CustomOfficeLayer> getCustomOfficeLayersByLayerLevel(Integer layerLevel) {
        return officeLayerDAO.getCustomOfficeLayersByLayerLevel(layerLevel);
    }

    public List<Office> getGRSenabledOfficesFromOffices(List<Long> officeIdList) {
        return this.officeDAO.getGRSenabledOfficesFromOffices(officeIdList);
    }

    public List<Long> getOfficeIdListByGeoDivisionId(Long geoDivisionId, Long layerLevel) {
        return this.officeDAO.getOfficeIdListByGeoDivisionId(geoDivisionId, layerLevel);
    }

    public List<Long> getOfficeIdListByGeoDistrictId(Long geoDistrictId, Long layerLevel) {
        return this.officeDAO.getOfficeIdListByGeoDistrictId(geoDistrictId, layerLevel);
    }

    public List<Office> getOfficesByLayerLevel(Integer layerLevel, Boolean grsEnabled) {
        return getOfficesByLayerLevel(layerLevel, grsEnabled, false);
    }

    public List<Office> getOfficesByNullableLayerLevelWithChildOffices(Integer layerLevel, Long firstSelection, Long secondSelection, Boolean grsEnabled) {
        if (layerLevel == null) return findAll();
        return getOfficesByLayerLevelWithChildOffices(layerLevel, firstSelection, secondSelection, grsEnabled);
    }

    public List<Office> getOfficesByLayerLevelWithChildOffices(Integer layerLevel, Long firstSelection, Long secondSelection, Boolean grsEnabled) {
        if (secondSelection != null) return Arrays.asList(getOffice(secondSelection));
        List<Office> officeList = getOfficesByLayerLevel(layerLevel, grsEnabled, false);
        if (firstSelection != null) {

            if (layerLevel == 0 || layerLevel == 1 || layerLevel == 2) {

            } else if (layerLevel == 3) {
                Integer intValue = firstSelection.intValue();
                return officeList
                        .stream()
                        .filter(office -> office.getOfficeLayer().getCustomLayerId().equals(intValue))
                        .collect(Collectors.toList());
            } else {
                return officeList
                        .stream()
                        .filter(office -> office.getOfficeOriginId().equals(firstSelection))
                        .collect(Collectors.toList());
            }
        }
        return officeList;
    }

    public OfficeInformation getCurrentLoggedInUserInformation() {
        HttpServletRequest request = messageService.getCurrentHttpRequest();
        Authentication authentication = TokenAuthenticationServiceUtil.getAuthentication(request);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return userInformation.getOfficeInformation();
    }

    public UserInformation getCurrentLoggedInUserInformationObject() {
        HttpServletRequest request = messageService.getCurrentHttpRequest();
        if (request == null)
            return UserInformation.builder()
                    .isCentralDashboardUser(false)
                    .build();

        Authentication authentication = TokenAuthenticationServiceUtil.getAuthentication(request);
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        return userInformation;
    }

    public List<Long> getListOfDescendantOfficeIds() {
        OfficeInformation officeInformation = getCurrentLoggedInUserInformation();
//        Long officeId = officeInformation.getOfficeId();
        Long officeMinistryId = officeInformation.getOfficeMinistryId();
        Long officeId = getOfficeIdOfMinistry(officeMinistryId);
        if (officeId == null) return new ArrayList<>(1);
        Map ministryOffices = CacheUtil.getMinistryDescendantOffices();
        List list = (List) ministryOffices.get(officeId);
        return list != null ? list : new ArrayList();
    }

    public Long getOfficeIdOfMinistry(Long officeMinistryId) {
        List<OfficeLayer> ministryLevelOfficeLayerList = getOfficeLayersByLayerLevelAndMinistryId(1, officeMinistryId);
        List<Office> ministryOffices = getOfficesByOfficeLayer(ministryLevelOfficeLayerList, false);
        return ministryOffices
                .stream()
                .findAny()
                .map(office -> office.getId())
                .orElse(null);
    }

    public List<Long> getListOfDescendantOfficeOriginIds() {
        OfficeInformation officeInformation = getCurrentLoggedInUserInformation();
        Long officeOriginId = officeInformation.getOfficeOriginId();
        Map ministryOfficeOrigins = CacheUtil.getMinistryDescendantOfficeOrigins();
        List list = (List) ministryOfficeOrigins.get(officeOriginId);
        return list != null ? list : new ArrayList();
    }

    public List<Office> getOfficesByLayerLevel(Integer layerLevel, Boolean grsEnabled, Boolean showChildOfficesOnly) {
        if (layerLevel == 0) {
            return new ArrayList<Office>() {{
                add(Office.builder().id(0L).nameBangla("অভিযোগ ব্যবস্থাপনা সেল").nameEnglish("Cell").build());
            }};
        }
        List<Office> offices = new ArrayList<>();
        List<OfficeLayer> officeLayers = getOfficeLayersUsingCustomLayer(layerLevel);
        List<Office> officesByOfficeLayers = getOfficesByOfficeLayer(officeLayers, grsEnabled);
        UserInformation userInformation = getCurrentLoggedInUserInformationObject();

        if (showChildOfficesOnly && (userInformation.getIsCentralDashboardUser() == null || !userInformation.getIsCentralDashboardUser())) {
            List<Long> childOfficesIds = getListOfDescendantOfficeIds();
            officesByOfficeLayers = officesByOfficeLayers.stream()
                    .filter(office -> childOfficesIds.contains(office.getId()))
                    .collect(Collectors.toList());
        }
        if (officesByOfficeLayers != null && officesByOfficeLayers.size() > 0) {
            offices.addAll(officesByOfficeLayers);
        }
        return offices;
    }

    public List<OfficeSearchDTO> getDescendantOfficeSearchingData() {
        List<OfficeSearchDTO> descendants = CacheUtil.getAllOfficeSearchDTOList();
        List<Long> childOfficesIds = getListOfDescendantOfficeIds();
        descendants = descendants.stream()
                .filter(d -> childOfficesIds.contains(d.getId()))
                .collect(Collectors.toList());
        return descendants;
    }


    public List<OfficeSearchDTO> getTopLayerOffices() {
        List<OfficeSearchDTO> descendants = CacheUtil.getAllOfficeSearchDTOList();
        descendants = descendants.stream()
                .filter(d -> d.getLayerLevel() != null && d.getLayerLevel() == 1)
                .collect(Collectors.toList());
        return descendants;
    }

    public List<OfficeSearchDTO> getGrsEnabledOfficeSearchingData() {
        return CacheUtil.getGrsEnabledOfficeSearchDTOList();
    }

    public List<OfficeSearchDTO> getOfficeSearchingData() {
        return CacheUtil.getAllOfficeSearchDTOList();
    }

    public List<Office> findAll() {
        return officeDAO.findAll();
    }

    public List<OfficeSearchDTO> generateOfficeSearchingData(boolean grsEnabled) {
        List<OfficeSearchDTO> list = new ArrayList();
        List<Office> officeList = officeDAO.findAll();
        List<CustomOfficeLayer> customOfficeLayers = officeLayerDAO.findAllCustomLayers();
        if (grsEnabled) {
            List<Long> officeIdsInOfficesGro = this.officesGroService.findAllOffficeIds();
            officeList = officeList.stream()
                    .filter(office -> officeIdsInOfficesGro.contains(office.getId()))
                    .collect(Collectors.toList());
        }
        officeList.stream().forEach(office -> {
            OfficeLayer officeLayer = office.getOfficeLayer();
            if (officeLayer == null) {
                return;
            }
            CustomOfficeLayer customOfficeLayer = customOfficeLayers.stream()
                    .filter(cl -> cl.getId().equals(officeLayer.getCustomLayerId() != null ? officeLayer.getCustomLayerId().longValue() : null))
                    .findFirst()
                    .orElse(null);

            Integer layerLevel = customOfficeLayer != null ? customOfficeLayer.getLayerLevel() : officeLayer.getLayerLevel();
            OfficeSearchDTO officeSearchDTO = OfficeSearchDTO.builder()
                    .id(office.getId())
                    .originId(office.getOfficeOriginId())
                    .layerId(officeLayer.getId())
                    .layerLevel(layerLevel)
                    .customLayerId(officeLayer.getCustomLayerId())
                    .nameBangla(office.getNameBangla())
                    .nameEnglish(office.getNameEnglish())
                    .build();
            list.add(officeSearchDTO);
        });
        return list;
    }

    public OfficeSearchContentsDTO getDropdownDataOnOfficeSearch(Integer layerLevel, Long officeOriginId, Integer customLayerId, Long officeId, boolean grsEnabled, boolean showChildOfficesOnly) {
        List<DropDownItemDTO> firstSelectionList = new ArrayList();
        List<DropDownItemDTO> secondSelectionList = new ArrayList();
        List<Office> officeList = new ArrayList();
        List<OfficeOrigin> officeOriginList = new ArrayList();
        if (layerLevel == 1 || layerLevel == 2 || layerLevel == 0) {
            officeList = getOfficesByLayerLevel(layerLevel, grsEnabled, showChildOfficesOnly);
        } else if (layerLevel == 3) {
            List<CustomOfficeLayer> customOfficeLayerList = officeLayerDAO.getCustomOfficeLayersByLayerLevel(layerLevel);
            officeList = getOfficesByLayerLevelAndCustomLayerId(layerLevel, customLayerId, grsEnabled, showChildOfficesOnly);
            firstSelectionList = customOfficeLayerList.stream().map(customOfficeLayer -> {
                Long id = customOfficeLayer.getId();
                return DropDownItemDTO.builder()
                        .id(id)
                        .nameBangla(customOfficeLayer.getName())
                        .nameEnglish(customOfficeLayer.getName())
                        .selected(id.equals(customLayerId.longValue()))
                        .build();
            }).collect(Collectors.toList());
        } else {
            officeOriginList = getOfficeOriginsByLayerLevel(layerLevel, grsEnabled);
            officeList = findByOfficeOriginId(officeOriginId, grsEnabled, showChildOfficesOnly);
            firstSelectionList = officeOriginList.stream().map(officeOrigin -> {
                Long id = officeOrigin.getId();
                return DropDownItemDTO.builder()
                        .id(id)
                        .nameBangla(officeOrigin.getOfficeNameBangla())
                        .nameEnglish(officeOrigin.getOfficeNameEnglish())
                        .selected(id.equals(officeOriginId))
                        .build();
            }).collect(Collectors.toList());
        }
        secondSelectionList = officeList.stream().map(office -> {
            Long id = office.getId();
            return DropDownItemDTO.builder()
                    .id(id)
                    .nameBangla(office.getNameBangla())
                    .nameEnglish(office.getNameEnglish())
                    .selected(id.equals(officeId))
                    .build();
        }).collect(Collectors.toList());

        return OfficeSearchContentsDTO.builder()
                .layerLevel(layerLevel)
                .firstSelectionList(firstSelectionList)
                .secondSelectionList(secondSelectionList)
                .build();
    }

    public List<Office> getDescendantOfficesByMinistryId(OfficeMinistry officeMinistry) {
        List<OfficeLayer> officeLayers = officeLayerDAO.findByMinistry(officeMinistry);
        List<Office> result = officeDAO.getOfficesByOfficeLayer(officeLayers);
        return result;
    }

    public WeakHashMap<String, WeakHashMap> generateDescendantOfficesIdListOfMinistries() {
        WeakHashMap<Long, List> officeIdMap = new WeakHashMap();
        WeakHashMap<Long, List> originIdMap = new WeakHashMap();
        List<Office> ministries = getOfficesByLayerLevel(1, false);
        ministries.stream().forEach(m -> {
            Long id = m.getId();
            Long originId = m.getOfficeOriginId();
            List<Office> descendantOffices = getDescendantOfficesByMinistryId(m.getOfficeMinistry());
            List<Long> officeIds = new ArrayList();
            Set<Long> originIds = new HashSet();
            officeIds.add(id);
            originIds.add(originId);
            for (Office o : descendantOffices) {
                officeIds.add(o.getId());
                originIds.add(o.getOfficeOriginId());
            }
            officeIdMap.put(id, officeIds);
            originIdMap.put(originId, originIds.stream().collect(Collectors.toList()));
        });
        return new WeakHashMap() {{
            put("officeIds", officeIdMap);
            put("originIds", originIdMap);
        }};
    }

    public List<Office> findByDivisionId(Integer divisionId) {
        return this.officeDAO.findByDivisionId(divisionId);
    }

    public List<Office> findByDivisionIdAndDistrictId(Integer divisionId, Integer districtId) {
        return this.officeDAO.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndUpazilaId(Integer divisionId, Integer districtId, Integer upazilaId) {
        return this.officeDAO.findByDivisionIdAndDistrictIdAndUpazilaId(divisionId, districtId, upazilaId);
    }

    public List<Office> findByDivisionIdAndOfficeMinistry(Integer divisionId, OfficeMinistry officeMinistry) {
        return this.officeDAO.findByDivisionIdAndOfficeMinistry(divisionId, officeMinistry);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndOfficeMinistry(Integer divisionId, Integer districtId, OfficeMinistry officeMinistry) {
        return this.officeDAO.findByDivisionIdAndDistrictIdAndOfficeMinistry(divisionId, districtId, officeMinistry);
    }

    public List<Office> findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(Integer divisionId, Integer districtId, Integer upazilaId, OfficeMinistry officeMinistry) {
        return this.officeDAO.findByDivisionIdAndDistrictIdAndUpazilaIdAndOfficeMinistry(divisionId, districtId, upazilaId, officeMinistry);
    }

    public List<EmployeeOffice> findByOfficeIdAndOfficeUnitOrganogramIdWIthoutStatus(Long officeId, Long officeUnitOrganogramId) {
        return this.employeeOfficeRepo.findByOfficeIdAndOfficeUnitOrganogramIdWIthoutStatus(officeId, officeUnitOrganogramId);
    }
}
