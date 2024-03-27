package com.grs.core.dao;

import com.grs.api.model.response.ServiceOriginDTO;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.repo.grs.ServiceOriginRepo;
import com.grs.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.grs.core.domain.grs.ServiceOrigin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceOriginDAO {
    @Autowired
    private ServiceOriginRepo serviceOriginRepo;

    public ServiceOrigin findOne(Long id) {
        return this.serviceOriginRepo.findOne(id);
    }

    public ServiceOrigin findById(Long id){
        return serviceOriginRepo.findOne(id);
    }

    public ServiceOrigin saveService(ServiceOrigin serviceOrigin){
        return serviceOriginRepo.save(serviceOrigin);
    }

    public Page<ServiceOriginDTO> findAll(Pageable pageable) {
        return serviceOriginRepo.findByOrderByIdAsc(pageable).map(this::convertToServiceOriginDTO);
    }

    public ServiceOriginDTO convertToServiceOriginDTO(ServiceOrigin serviceOrigin) {
        return ServiceOriginDTO.builder()
                .id(serviceOrigin.getId())
                .officeOriginId(serviceOrigin.getOfficeOriginId())
                .officeOriginUnitId(serviceOrigin.getOfficeOriginUnitId())
                .officeOriginUnitOrganogramId(serviceOrigin.getOfficeOriginUnitOrganogramId())
                .serviceNameBangla(serviceOrigin.getServiceNameBangla())
                .serviceNameEnglish(serviceOrigin.getServiceNameEnglish())
                .serviceProcedureBangla(serviceOrigin.getServiceProcedureBangla())
                .serviceProcedureEnglish(serviceOrigin.getServiceProcedureEnglish())
                .documentAndLocationBangla(serviceOrigin.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(serviceOrigin.getDocumentAndLocationEnglish())
                .paymentMethodBangla(serviceOrigin.getPaymentMethodBangla())
                .paymentMethodEnglish(serviceOrigin.getPaymentMethodEnglish())
                .serviceTime(serviceOrigin.getServiceTime())
                .serviceType(serviceOrigin.getServiceType())
                .status(serviceOrigin.getStatus())
                .build();
    }

    public ServiceOrigin convertToServiceOrigin(ServiceOriginDTO serviceOriginDTO) {
        return ServiceOrigin.builder()
                .id(serviceOriginDTO.getId())
                .serviceNameBangla(serviceOriginDTO.getServiceNameBangla())
                .serviceNameEnglish(serviceOriginDTO.getServiceNameEnglish())
                .serviceProcedureBangla(serviceOriginDTO.getServiceProcedureBangla())
                .serviceProcedureEnglish(serviceOriginDTO.getServiceProcedureEnglish())
                .documentAndLocationBangla(serviceOriginDTO.getDocumentAndLocationBangla())
                .documentAndLocationEnglish(serviceOriginDTO.getDocumentAndLocationEnglish())
                .paymentMethodBangla(serviceOriginDTO.getPaymentMethodBangla())
                .paymentMethodEnglish(serviceOriginDTO.getPaymentMethodEnglish())
                .serviceTime(serviceOriginDTO.getServiceTime())
                .serviceType(serviceOriginDTO.getServiceType())
                .build();
    }

    public List<ServiceOrigin> saveAll(List<ServiceOrigin> serviceOriginList) {
        return this.serviceOriginRepo.save(serviceOriginList);
    }

    public List<ServiceOriginDTO> findAllServicesByOfficeOrigin(Long officeOriginId) {
        List<ServiceOriginDTO> serviceOriginDTOS = new ArrayList<>();
        List<ServiceOrigin> serviceOrigins = serviceOriginRepo.findByOfficeOriginId(officeOriginId);
        serviceOrigins.forEach(serviceOrigin -> {
            serviceOriginDTOS.add(convertToServiceOriginDTO(serviceOrigin));
        });
        return serviceOriginDTOS;
    }

    public List<ServiceOriginDTO> findAllServiceOriginDTO(Long officeOriginId) {
        return serviceOriginRepo.findByOfficeOriginId(officeOriginId).stream()
                .map(this::convertToServiceOriginDTO)
                .collect(Collectors.toList());
    }

    public ServiceOrigin findByServiceId(Long serviceId) {
        return this.serviceOriginRepo.findById(serviceId);
    }

}
