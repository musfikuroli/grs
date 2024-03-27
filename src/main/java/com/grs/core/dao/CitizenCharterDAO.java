package com.grs.core.dao;

import com.grs.api.model.response.CitizenCharterDTO;
import com.grs.api.model.response.dashboard.GrievanceCountByItemDTO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.grs.CitizenCharterRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 9/12/2017.
 */
@Service
public class CitizenCharterDAO {
    @Autowired
    private CitizenCharterRepo citizenCharterRepo;

    public CitizenCharter findOne(Long id) {
        return this.citizenCharterRepo.findOne(id);
    }

    public List<CitizenCharter> findByOffice(Long officeId) {
        return this.citizenCharterRepo.findByOfficeId(officeId);
    }

    public List<CitizenCharter> findByOfficeAndServiceOriginServiceType(Long officeId, ServiceType serviceType) {
        return this.citizenCharterRepo.findByOfficeIdAndServiceOriginServiceTypeAndStatusTrueAndOriginStatusTrue(officeId, serviceType);
    }

    public CitizenCharter findByOfficeAndService(Long officeId, ServiceOrigin serviceOrigin) {
        return this.citizenCharterRepo.findByOfficeIdAndServiceOrigin(officeId, serviceOrigin);
    }

    public CitizenCharter findByOfficeIdAndServiceId(Long officeId, Long serviceOriginId) {
        return this.citizenCharterRepo.findByOfficeIdAndServiceOriginId(officeId, serviceOriginId);
    }

    public Page<CitizenCharterDTO> findAll(Pageable pageable, Long officeId) {
        return citizenCharterRepo.findByOfficeIdOrderByIdAsc(officeId, pageable).map(this::convertToCitizenCharterDTO);
    }

    public CitizenCharter saveCitizenCharter(CitizenCharter citizenCharter) {
        return this.citizenCharterRepo.save(citizenCharter);
    }

    public List<CitizenCharter> saveAllCitizenCharters(List<CitizenCharter> citizenCharters) {
        return this.citizenCharterRepo.save(citizenCharters);
    }

    public CitizenCharterDTO convertToCitizenCharterDTO(CitizenCharter citizenCharter) {
        Long serviceId = null;
        if (citizenCharter.getServiceOrigin() != null) {
            serviceId = citizenCharter.getServiceOrigin().getId();
        }
        Long officeId = null;
        if (citizenCharter.getOfficeId() != null) {
            officeId = citizenCharter.getOfficeId();
        }
        Long officeUnitOrganogramId = citizenCharter.getSoOfficeUnitOrganogramId();

        return CitizenCharterDTO.builder()
                .id(citizenCharter.getId())
                .serviceId(serviceId)
                .officeId(officeId)
                .officeUnitOrganogramId(officeUnitOrganogramId)
                .serviceNameBangla(citizenCharter.getServiceNameBangla())
                .serviceNameEnglish(citizenCharter.getServiceNameEnglish())
                .serviceProcedureBangla(citizenCharter.getServiceProcedureBangla())
                .serviceProcedureEnglish(citizenCharter.getServiceProcedureEnglish())
                .documentAndLocationBangla(citizenCharter.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(citizenCharter.getDocumentAndLocationEnglish())
                .paymentMethodBangla(citizenCharter.getPaymentMethodBangla())
                .paymentMethodEnglish(citizenCharter.getPaymentMethodEnglish())
                .serviceTime(citizenCharter.getServiceTime())
                .status(citizenCharter.getStatus())
                .build();
    }

    public CitizenCharter convertToCitizenCharter(CitizenCharterDTO citizenCharterDTO, Office office, ServiceOrigin serviceOrigin) {
        return CitizenCharter.builder()
                .id(citizenCharterDTO.getId())
                .soOfficeId(citizenCharterDTO.getSoOfficeId())
                .soOfficeUnitId(citizenCharterDTO.getSoOfficeUnitId())
                .soOfficeUnitOrganogramId(citizenCharterDTO.getOfficeUnitOrganogramId())
                .officeId(citizenCharterDTO.getOfficeId())
                .officeOriginId(citizenCharterDTO.getOfficeOriginId())
                .serviceNameBangla(citizenCharterDTO.getServiceNameBangla())
                .serviceNameEnglish(citizenCharterDTO.getServiceNameEnglish())
                .serviceProcedureBangla(citizenCharterDTO.getServiceProcedureBangla())
                .serviceProcedureEnglish(citizenCharterDTO.getServiceProcedureEnglish())
                .documentAndLocationBangla(citizenCharterDTO.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(citizenCharterDTO.getDocumentAndLocationEnglish())
                .paymentMethodBangla(citizenCharterDTO.getPaymentMethodBangla())
                .paymentMethodEnglish(citizenCharterDTO.getPaymentMethodEnglish())
                .serviceTime(citizenCharterDTO.getServiceTime())
                .officeId(office.getId())
                .serviceOrigin(serviceOrigin)
                //.officeUnitOrganogramId(officeUnitOrganogram.getId())
                .status(citizenCharterDTO.getStatus())
                .build();
    }

    public List<GrievanceCountByItemDTO> getGrievanceCountByServices(Long officeId) {
        List<CitizenCharter> citizenCharters = this.findByOffice(officeId);
        return citizenCharters.stream()
                .map(this::convertToGrievanceCountByItemDTO)
                .collect(Collectors.toList());
    }

    public GrievanceCountByItemDTO convertToGrievanceCountByItemDTO(CitizenCharter citizenCharter) {
        return GrievanceCountByItemDTO.builder()
                .id(citizenCharter.getId())
                .nameBangla(citizenCharter.getServiceNameBangla())
                .nameEnglish(citizenCharter.getServiceNameEnglish())
                .grievanceCount(0L)
                .build();
    }

    public Integer countByServiceIdAndOfficeId(Long serviceId, Long officeId) {
        return citizenCharterRepo.countByServiceIdAndOfficeId(serviceId, officeId);
    }

    public List<CitizenCharter> getCitizenChartersByServiceOrigin(ServiceOrigin serviceOrigin) {
        return citizenCharterRepo.findByServiceOrigin(serviceOrigin);
    }

    public CitizenCharter findByServiceIdAndOfficeId(Long serviceId, Long officeId) {
        return this.citizenCharterRepo.findByServiceIdAndOfficeId(serviceId, officeId);
    }

    public List<CitizenCharter> getCitizenCharterByListOfId(List<Long> idList) {
        return citizenCharterRepo.findByIdIn(idList);
    }

}
