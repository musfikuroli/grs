package com.grs.core.service;

import com.grs.api.model.request.GenericCitizenCharterUploaderRequestDTO;
import com.grs.api.model.response.GenericResponse;
import com.grs.core.dao.CitizenCharterOriginDAO;
import com.grs.core.dao.GRSSettingsDAO;
import com.grs.core.dao.ServiceOriginDAO;
import com.grs.core.domain.ServiceType;
import com.grs.core.domain.grs.CitizensCharterOrigin;
import com.grs.core.domain.grs.Education;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.domain.grs.ServiceOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Aftab on 2/20/2018.
 */
@Service
public class GRSSettingsService {
    @Autowired
    private GRSSettingsDAO grsSettingsDAO;
    @Autowired
    private CitizenCharterOriginDAO citizenCharterOriginDAO;
    @Autowired
    private ServiceOriginDAO serviceOriginDAO;

    boolean flag = true;

    public List<Occupation> getOccupations() {
        return this.grsSettingsDAO.getOccupations();
    }

    public List<Occupation> getActiveOccupations() {
        return this.grsSettingsDAO.getActiveOccupations();
    }

    public List<Education> getEducationalQualifications() {
        return this.grsSettingsDAO.getEducationalQualifications();
    }

    public List<Education> getActiveEducationalQualifications() {
        return this.grsSettingsDAO.getActiveEducationalQualifications();
    }

    @Transactional("transactionManager")
    public GenericResponse uploadCitizenCharter(GenericCitizenCharterUploaderRequestDTO citizenCharterUploaderRequestDTO) {
        StringBuilder stringBuilder = new StringBuilder("");
        citizenCharterUploaderRequestDTO.getExpectationsBangla().forEach(item-> {
            stringBuilder.append(item+"\n");
        });
        String expectationBng= stringBuilder.toString();
        stringBuilder.delete(0, stringBuilder.length());
        citizenCharterUploaderRequestDTO.getExpectationsEnglish().forEach(item-> {
            stringBuilder.append(item+"\n");
        });
        String expectationEng = stringBuilder.toString();

        CitizensCharterOrigin citizenCharterOrigin = citizenCharterOriginDAO.findByOfficeOriginId(citizenCharterUploaderRequestDTO.getOfficeOriginId());
        if(citizenCharterOrigin == null) {
            citizenCharterOrigin = CitizensCharterOrigin.builder().build();
        }
        citizenCharterOrigin.setOfficeOriginId(citizenCharterUploaderRequestDTO.getOfficeOriginId());
        citizenCharterOrigin.setOfficeOriginNameBangla(citizenCharterUploaderRequestDTO.getOfficeOriginNameBangla());
        citizenCharterOrigin.setOfficeOriginNameEnglish(citizenCharterUploaderRequestDTO.getOfficeOriginNameEnglish());
        citizenCharterOrigin.setLayerLevel(citizenCharterUploaderRequestDTO.getLayerLevel());
        citizenCharterOrigin.setVisionBangla(citizenCharterUploaderRequestDTO.getVisionBangla());
        citizenCharterOrigin.setVisionEnglish(citizenCharterUploaderRequestDTO.getVisionEnglish());
        citizenCharterOrigin.setMissionBangla(citizenCharterUploaderRequestDTO.getMissionBangla());
        citizenCharterOrigin.setMissionEnglish(citizenCharterUploaderRequestDTO.getMissionEnglish());
        citizenCharterOrigin.setExpectationBangla(expectationBng);
        citizenCharterOrigin.setExpectationEnglish(expectationEng);
        citizenCharterOriginDAO.save(citizenCharterOrigin);

        List<ServiceOrigin> serviceOriginList = citizenCharterUploaderRequestDTO.getCitizenCharters()
                .stream()
                .map(x->{
                    if(x.getService().getNameBangla() == null || x.getService().getServiceTime() == null || x.getService().getServiceType() == null){
                        flag = false;
                    }

                    ServiceOrigin serviceOrigin = ServiceOrigin.builder()
                            .serviceNameBangla(x.getService().getNameBangla())
                            .serviceNameEnglish(x.getService().getNameEnglish())
                            .serviceProcedureBangla(x.getService().getServingProcessBangla())
                            .serviceProcedureEnglish(x.getService().getServingProcessEnglish())
                            .documentAndLocationBangla(x.getService().getDocumentAndLocationBangla())
                            .documentAndLocationEnglish(x.getService().getDocumentAndLocationEnglish())
                            .paymentMethodBangla(x.getService().getPaymentMethodBangla())
                            .paymentMethodEnglish(x.getService().getPaymentMethodEnglish())
                            .serviceTime(x.getService().getServiceTime())
                            .officeOriginId(x.getServiceOfficer().getOfficeOriginId())
                            .officeOriginUnitId(x.getServiceOfficer().getOfficeOriginUnitId())
                            .officeOriginUnitOrganogramId(x.getServiceOfficer().getOfficeOriginUnitOrganogramId())
                            .serviceType(ServiceType.valueOf(x.getService().getServiceType()))
                            .build();
                    serviceOrigin.setStatus(true);
                    return serviceOrigin;
                }).collect(Collectors.toList());
        List<ServiceOrigin> serviceOriginResponseList = new ArrayList();
        for(ServiceOrigin serviceOrigin : serviceOriginList) {
            serviceOriginResponseList.add(serviceOriginDAO.saveService(serviceOrigin));
        }
        if(flag) {
            return GenericResponse.builder().success(true).message("বিঃ দ্রঃ আপলোড করা সেবাসমূহ সেবা প্রদান প্রতিশ্রুতিতে দেখতে \"GRS এ দপ্তর সেটআপ\" অপশন থেকে সেবাপ্রদানকারী কর্মকর্তাগণের তথ্য সঠিক কিনা অনুগ্রহপূর্বক তা নিশ্চিত করুন").build();
        } else {
            return GenericResponse.builder().success(false).message("প্রক্রিয়াকরণ ব্যাহত হওয়ায় সেবা প্রদান প্রতিশ্রুতি আপলোড বিঘ্নিত হয়েছে").build();
        }
    }
}
