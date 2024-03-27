package com.grs.core.dao;

import com.grs.api.model.GrsRoleToEmailDTO;
import com.grs.core.domain.grs.EmailTemplate;
import com.grs.core.domain.grs.GrsRole;
import com.grs.core.domain.grs.GrsRoleToEmail;
import com.grs.core.repo.grs.GrsRoleToEmailRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by HP on 3/11/2018.
 */
@Service
public class GrsRoleToEmailDAO {
    @Autowired
    private EmailTemplateDAO emailTemplateDAO;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private GrsRoleToEmailRepo grsRoleToEmailRepo;

    public List<GrsRoleToEmailDTO> findByEmailTemplate(EmailTemplate emailTemplate) {
        List<GrsRoleToEmailDTO> grsRoleToEmailDTOList = this.grsRoleToEmailRepo.findByEmailTemplate(emailTemplate)
                .stream()
                .map(this::convertToGrsRoleToEmailDTO)
                .collect(Collectors.toList());
        return grsRoleToEmailDTOList;
    }


    public List<GrsRoleToEmailDTO> findByEmailTemplateAndStatus(EmailTemplate emailTemplate, Boolean status) {
        List<GrsRoleToEmailDTO> grsRoleToEmailDTOList = this.grsRoleToEmailRepo.findByEmailTemplateAndStatus(emailTemplate, status).stream()
                .map(this::convertToGrsRoleToEmailDTO)
                .collect(Collectors.toList());
        return grsRoleToEmailDTOList;
    }

    public GrsRoleToEmailDTO convertToGrsRoleToEmailDTO(GrsRoleToEmail grsRoleToEmail) {
        return GrsRoleToEmailDTO.builder()
                .id(grsRoleToEmail.getId())
                .emailTemplateId(grsRoleToEmail.getEmailTemplate().getId())
                .grsRole(grsRoleToEmail.getGrsRole().toString())
                .status(grsRoleToEmail.getStatus())
                .build();
    }

    public GrsRoleToEmail convertToGrsRoleToEmail(GrsRoleToEmailDTO grsRoleToEmailDTO) {
        Long id = grsRoleToEmailDTO.getId();
        EmailTemplate emailTemplate = this.emailTemplateDAO.findOne(grsRoleToEmailDTO.getEmailTemplateId());
        GrsRole grsRole = this.grsRoleDAO.findByRole(grsRoleToEmailDTO.getGrsRole());
        return GrsRoleToEmail.builder()
                .id(id)
                .status(grsRoleToEmailDTO.getStatus())
                .emailTemplate(emailTemplate)
                .grsRole(grsRole.getRole())
                .build();
    }

    public void saveEmailRecipient(GrsRoleToEmailDTO grsRoleToEmailDTO) {
        GrsRoleToEmail grsRoleToEmail = this.convertToGrsRoleToEmail(grsRoleToEmailDTO);
        this.grsRoleToEmailRepo.save(grsRoleToEmail);
    }

    public void saveAll(List<GrsRoleToEmailDTO> grsRoleToEmailDTOList) {
        List<GrsRoleToEmail> grsRoleToEmailList = grsRoleToEmailDTOList.stream()
                .map(this::convertToGrsRoleToEmail)
                .collect(Collectors.toList());
        this.grsRoleToEmailRepo.save(grsRoleToEmailList);
    }
}
