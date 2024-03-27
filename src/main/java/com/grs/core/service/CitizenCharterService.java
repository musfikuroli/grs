package com.grs.core.service;

import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.response.*;
import com.grs.core.dao.CitizenCharterDAO;
import com.grs.core.dao.ServiceOriginDAO;
import com.grs.core.domain.ServicePair;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.domain.projapoti.Office;
import com.grs.utils.BanglaConverter;
import com.grs.utils.CookieUtil;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 05-Oct-17.
 */
@Slf4j
@Service
public class CitizenCharterService {
    @Autowired
    private CitizenCharterDAO citizenCharterDAO;
    @Autowired
    private OfficeService officeService;
    @Autowired
    private ServiceOriginDAO serviceOriginDAO;
    @Autowired
    private MessageService messageService;

    public CitizenCharter findOne(Long id) {
        return this.citizenCharterDAO.findOne(id);
    }

    public CitizenCharter findByOfficeAndService(Long officeId, ServiceOrigin serviceOrigin) {
        return citizenCharterDAO.findByOfficeAndService(officeId, serviceOrigin);
    }

    public Boolean saveService(ServiceOriginDTO serviceOriginDTO) {
        try {
            ServiceOrigin serviceOrigin = serviceOriginDAO.convertToServiceOrigin(serviceOriginDTO);
            serviceOriginDAO.saveService(serviceOrigin);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public GenericResponse validateServiceOriginInput(ServiceOriginDTO serviceOriginDTO) {
        String message = null;
        Boolean success = true;
        if (!StringUtil.isValidString(serviceOriginDTO.getServiceNameBangla())) {
            message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "service.name");
        }
        if (serviceOriginDTO.getOfficeOriginUnitId() == null || serviceOriginDTO.getOfficeOriginUnitId() == 0L) {
            message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "section");
        }
        if (serviceOriginDTO.getOfficeOriginUnitOrganogramId() == null || serviceOriginDTO.getOfficeOriginUnitOrganogramId() == 0L) {
            message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "designation");
        }
        if (serviceOriginDTO.getServiceTime() == null) {
            message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "service.time");
        } else if (serviceOriginDTO.getServiceTime() <= 0) {
            message = messageService.getMessageWithArgsV2("x.should.be.greater.than.y", "service.time", "zero");
        }
        if (serviceOriginDTO.getStatus() == null) {
            message = messageService.getMessageWithArgsV2("x.cannot.be.blank", "status");
        }
        if (message != null) {
            success = false;
        }
        return GenericResponse.builder().success(success).message(message).build();
    }

    @Transactional("transactionManager")
    public GenericResponse saveServiceOrigin(ServiceOriginDTO serviceOriginDTO) {
        GenericResponse validationResponse = validateServiceOriginInput(serviceOriginDTO);
        if (!validationResponse.success) {
            return validationResponse;
        }
        ServiceOrigin serviceOrigin = null;
        Long id = serviceOriginDTO.getId();
        if (id != null && id > 0) {
            serviceOrigin = serviceOriginDAO.findOne(id);
        } else {
            serviceOrigin = ServiceOrigin.builder().build();
        }
        serviceOrigin.setId(serviceOriginDTO.getId());
        serviceOrigin.setOfficeOriginId(serviceOriginDTO.getOfficeOriginId());
        serviceOrigin.setOfficeOriginUnitId(serviceOriginDTO.getOfficeOriginUnitId());
        serviceOrigin.setOfficeOriginUnitOrganogramId(serviceOriginDTO.getOfficeOriginUnitOrganogramId());
        serviceOrigin.setServiceNameBangla(serviceOriginDTO.getServiceNameBangla());
        serviceOrigin.setServiceNameEnglish(serviceOriginDTO.getServiceNameEnglish());
        serviceOrigin.setServiceProcedureBangla(serviceOriginDTO.getServiceProcedureBangla());
        serviceOrigin.setServiceProcedureEnglish(serviceOriginDTO.getServiceProcedureEnglish());
        serviceOrigin.setDocumentAndLocationBangla(serviceOriginDTO.getDocumentAndLocationBangla());
        serviceOrigin.setDocumentAndLocationEnglish(serviceOriginDTO.getDocumentAndLocationEnglish());
        serviceOrigin.setPaymentMethodBangla(serviceOriginDTO.getPaymentMethodBangla());
        serviceOrigin.setPaymentMethodEnglish(serviceOriginDTO.getPaymentMethodEnglish());
        serviceOrigin.setServiceTime(serviceOriginDTO.getServiceTime());
        serviceOrigin.setServiceType(serviceOriginDTO.getServiceType());
        serviceOrigin.setStatus(serviceOriginDTO.getStatus());
        serviceOrigin = serviceOriginDAO.saveService(serviceOrigin);
        List<CitizenCharter> citizenCharterList = citizenCharterDAO.getCitizenChartersByServiceOrigin(serviceOrigin);
        if(citizenCharterList.size() > 0) {
            for(CitizenCharter citizenCharter: citizenCharterList) {
                citizenCharter.setServiceNameBangla(serviceOrigin.getServiceNameBangla());
                citizenCharter.setServiceNameEnglish(serviceOrigin.getServiceNameEnglish());
                citizenCharter.setServiceProcedureBangla(serviceOrigin.getServiceProcedureBangla());
                citizenCharter.setServiceProcedureEnglish(serviceOrigin.getServiceProcedureEnglish());
                citizenCharter.setDocumentAndLocationBangla(serviceOrigin.getDocumentAndLocationBangla());
                citizenCharter.setDocumentAndLocationEnglish(serviceOrigin.getDocumentAndLocationEnglish());
                citizenCharter.setPaymentMethodBangla(serviceOrigin.getPaymentMethodBangla());
                citizenCharter.setPaymentMethodEnglish(serviceOrigin.getPaymentMethodEnglish());
                citizenCharter.setServiceTime(serviceOrigin.getServiceTime());
                citizenCharter.setStatus(serviceOrigin.getStatus());
                citizenCharter.setOriginStatus(serviceOrigin.getStatus());
            }
            citizenCharterDAO.saveAllCitizenCharters(citizenCharterList);
        }
        Boolean flag = (serviceOrigin != null);
        String code = flag ? "x.save.success" : "can.not.save.x";
        String message = messageService.getMessageWithArgsV2(code, "service");
        return GenericResponse.builder()
                .success(flag)
                .message(message)
                .build();
    }

    public List<ServicePair> getAllowedServiceTypes(Authentication authentication, HttpServletRequest request) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        String languageCode = CookieUtil.getValue(request, "lang");
        String publicService;
        String officeService;
        String staffService;
        if (languageCode == null || languageCode.equals("fr")) {
            publicService = BanglaConverter.convertServiceTypeToBangla(ServiceType.NAGORIK);
            officeService = BanglaConverter.convertServiceTypeToBangla(ServiceType.DAPTORIK);
            staffService = BanglaConverter.convertServiceTypeToBangla(ServiceType.STAFF);
        } else {
            publicService = "Public";
            officeService = "Official";
            staffService = "Stuff";
        }
        List<ServicePair> servicePairs = new ArrayList<>();
        if (userInformation.getUserType() == UserType.COMPLAINANT) {
            servicePairs.add(new ServicePair(ServiceType.NAGORIK, publicService));
        } else {
            servicePairs.add(new ServicePair(ServiceType.NAGORIK, publicService));
            servicePairs.add(new ServicePair(ServiceType.STAFF, staffService));
            servicePairs.add(new ServicePair(ServiceType.DAPTORIK, officeService));
        }
        return servicePairs;
    }

    public List<ServicePair> getDefaultAllowedServiceTypes(HttpServletRequest request) {
        String languageCode = CookieUtil.getValue(request, "lang");
        String publicService;
        String officeService;
        String staffService;
        if (languageCode == null || languageCode.equals("fr")) {
            publicService = BanglaConverter.convertServiceTypeToBangla(ServiceType.NAGORIK);
            officeService = BanglaConverter.convertServiceTypeToBangla(ServiceType.DAPTORIK);
            staffService = BanglaConverter.convertServiceTypeToBangla(ServiceType.STAFF);
        } else {
            publicService = "Public";
            officeService = "Official";
            staffService = "Stuff";
        }
        List<ServicePair> servicePairs = new ArrayList<>();
        servicePairs.add(new ServicePair(ServiceType.NAGORIK, publicService));
        servicePairs.add(new ServicePair(ServiceType.STAFF, staffService));
        servicePairs.add(new ServicePair(ServiceType.DAPTORIK, officeService));
        return servicePairs;
    }

    public Boolean updateCitizenCharter(Long officeServiceID, CitizenCharterDTO citizenCharterDTO) {
        try {
            CitizenCharter existingCharter = citizenCharterDAO.findByOfficeAndService(
                    citizenCharterDTO.getOfficeId(),
                    this.serviceOriginDAO.findById(citizenCharterDTO.getServiceId())
            );
            citizenCharterDAO.saveCitizenCharter(existingCharter);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public List<ServiceStatusInOfficeDTO> getCitizenCharterListByServiceOriginId(Long serviceOriginId) {
        ServiceOrigin serviceOrigin = serviceOriginDAO.findOne(serviceOriginId);
        List<CitizenCharter> citizenCharterList = citizenCharterDAO.getCitizenChartersByServiceOrigin(serviceOrigin);
        List<Long> officeIdList = citizenCharterList.stream()
                .map(CitizenCharter::getOfficeId)
                .collect(Collectors.toList());
        List<Office> serviceOriginUserOffices = officeService.findByIdContainsInList(officeIdList);
        List<ServiceStatusInOfficeDTO> serviceStatusInOfficeDTOs = new ArrayList();
        for (CitizenCharter citizenCharter : citizenCharterList) {
            Office office = serviceOriginUserOffices.stream().filter(o -> o.getId().equals(citizenCharter.getOfficeId())).findFirst().orElse(null);
            serviceStatusInOfficeDTOs.add(ServiceStatusInOfficeDTO.builder()
                    .Id(citizenCharter.getId())
                    .officeNameBangla(office == null ? "" : office.getNameBangla())
                    .officeNameEnglish(office == null ? "" : office.getNameEnglish())
                    .status(citizenCharter.getOriginStatus())
                    .build());
        }
        return serviceStatusInOfficeDTOs;
    }

    @Transactional("transactionManager")
    public Boolean updateServiceUserOfficesStatus(List<ItemIdStatusDTO> idStatusList) {
        try {
            List<Long> citizensCharterIdList = idStatusList.stream().map(ItemIdStatusDTO::getId).collect(Collectors.toList());
            List<CitizenCharter> citizenCharterList = citizenCharterDAO.getCitizenCharterByListOfId(citizensCharterIdList);
            for (CitizenCharter citizenCharter : citizenCharterList) {
                ItemIdStatusDTO idStatus = idStatusList.stream().filter(o -> o.getId().equals(citizenCharter.getId())).findFirst().get();
                if (citizenCharter.getStatus() != idStatus.getStatus()) {
                    citizenCharter.setOriginStatus(idStatus.getStatus());
                    citizenCharter.setStatus(idStatus.getStatus());
                    citizenCharterDAO.saveCitizenCharter(citizenCharter);
                }
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Boolean copyService(ServiceOriginDTO serviceOriginDTO, Long officeId) {
        CitizenCharter citizenCharter = new CitizenCharter();
        citizenCharter.setOfficeId(officeId);
        citizenCharter.setServiceNameBangla(serviceOriginDTO.getServiceNameBangla());
        citizenCharter.setServiceNameEnglish(serviceOriginDTO.getServiceNameEnglish());
        citizenCharter.setServiceProcedureEnglish(serviceOriginDTO.getServiceProcedureEnglish());
        citizenCharter.setServiceProcedureBangla(serviceOriginDTO.getServiceProcedureBangla());
        citizenCharter.setPaymentMethodBangla(serviceOriginDTO.getPaymentMethodBangla());
        citizenCharter.setPaymentMethodEnglish(serviceOriginDTO.getPaymentMethodEnglish());
        citizenCharter.setDocumentAndLocationEnglish(serviceOriginDTO.getDocumentAndLocationEnglish());
        citizenCharter.setDocumentAndLocationBangla(serviceOriginDTO.getDocumentAndLocationBangla());
        citizenCharter.setServiceTime(serviceOriginDTO.getServiceTime());
        citizenCharter.setStatus(serviceOriginDTO.getStatus());
        citizenCharterDAO.saveCitizenCharter(citizenCharter);
        return true;
    }

    public CitizenCharter findByOfficeIdAndServiceId(Long officeId, Long serviceId) {
        return citizenCharterDAO.findByOfficeIdAndServiceId(officeId, serviceId);
    }

    public CitizenCharter saveCitizenCharter(CitizenCharter citizenCharter) {
        return this.citizenCharterDAO.saveCitizenCharter(citizenCharter);
    }

    public CitizenCharterDTO getDTOFromCitizenCharter(CitizenCharter citizenCharter){
        return this.citizenCharterDAO.convertToCitizenCharterDTO(citizenCharter);
    }
}
