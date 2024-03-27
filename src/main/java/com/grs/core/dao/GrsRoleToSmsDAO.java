package com.grs.core.dao;

import com.grs.api.model.GrsRoleToSmsDTO;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.grs.GrsRoleToSms;
import com.grs.core.domain.grs.SmsTemplate;
import com.grs.core.repo.grs.GrsRoleToSmsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by HP on 3/12/2018.
 */
@Service
public class GrsRoleToSmsDAO {
    @Autowired
    private SmsTemplateDAO smsTemplateDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private GrsRoleToSmsRepo grsRoleToSmsRepo;

    public GrsRoleToSms convertToGrsRoleToSms(GrsRoleToSmsDTO grsRoleToSmsDTO) {
        Long id = grsRoleToSmsDTO.getId();
        SmsTemplate smsTemplate = this.smsTemplateDAO.findOne(grsRoleToSmsDTO.getSmsTemplateId());
        GrsRole grsRole = this.grsRoleDAO.findByRole(grsRoleToSmsDTO.getGrsRole());
        return GrsRoleToSms.builder()
                .id(id)
                .status(grsRoleToSmsDTO.getStatus())
                .smsTemplate(smsTemplate)
                .grsRole(grsRole.getRole())
                .build();
    }

    public void saveSmsRecipient(GrsRoleToSmsDTO grsRoleToSmsDTO) {
        GrsRoleToSms grsRoleToSms = this.convertToGrsRoleToSms(grsRoleToSmsDTO);
        this.grsRoleToSmsRepo.save(grsRoleToSms);
    }

    public List<GrsRoleToSmsDTO> findBySmsTemplateAndStatus(SmsTemplate smsTemplate, Boolean status) {
        List<GrsRoleToSmsDTO> grsRoleToSmsDTOList = this.grsRoleToSmsRepo.findBySmsTemplateAndStatus(smsTemplate, status).stream()
                .map(this::convertToGrsRoleToSmsDTO)
                .collect(Collectors.toList());
        return grsRoleToSmsDTOList;
    }

    public GrsRoleToSmsDTO convertToGrsRoleToSmsDTO(GrsRoleToSms grsRoleToSms) {

        return GrsRoleToSmsDTO.builder()
                .id(grsRoleToSms.getId())
                .smsTemplateId(grsRoleToSms.getSmsTemplate().getId())
                .grsRole(grsRoleToSms.getGrsRole().toString())
                .status(grsRoleToSms.getStatus())
                .build();
    }

    public List<GrsRoleToSmsDTO> findBySmsTemplate(SmsTemplate smsTemplate) {
        List<GrsRoleToSmsDTO> grsRoleToSmsDTOList = this.grsRoleToSmsRepo.findBySmsTemplate(smsTemplate)
                .stream()
                .map(this::convertToGrsRoleToSmsDTO)
                .collect(Collectors.toList());
        return grsRoleToSmsDTOList;
    }

    public void saveAll(List<GrsRoleToSmsDTO> grsRoleToSmsDTOList) {
        List<GrsRoleToSms> grsRoleToEmailList = grsRoleToSmsDTOList.stream()
                .map(this::convertToGrsRoleToSms)
                .collect(Collectors.toList());
        this.grsRoleToSmsRepo.save(grsRoleToEmailList);
    }
}
