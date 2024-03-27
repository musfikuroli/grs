package com.grs.core.service;

import com.grs.api.model.request.GrsUserDTO;
import com.grs.core.dao.GrsRoleDAO;
import com.grs.core.dao.SuperAdminDAO;
import com.grs.core.domain.grs.SuperAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 1/30/2018.
 */
@Service
public class GrsUserService {

    @Autowired
    private ComplainantService complainantService;
    @Autowired
    private GrsRoleDAO grsRoleDAO;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private SuperAdminDAO superAdminDAO;

    public GrsUserDTO convertToSuperAdminDTO(SuperAdmin superAdmin) {
        return GrsUserDTO.builder()
                .Id(superAdmin.getId())
                .username(superAdmin.getUsername())
                .password(superAdmin.getPassword())
                .email(superAdmin.getEmail())
                .mobileNumber(superAdmin.getPhoneNumber())
                .build();
    }

    public SuperAdmin save(SuperAdmin superAdmin) {
        return this.superAdminDAO.save(superAdmin);
    }

    public Boolean register(GrsUserDTO grsUserDTO) {

        Integer countEducation = this.superAdminDAO.countByUsername(grsUserDTO.getUsername());
        if (!countEducation.equals(0)){
            return false;
        }

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        String pinNumber = this.complainantService.getRandomPinNumber();
        grsUserDTO.setPassword(pinNumber);

        SuperAdmin superAdmin = SuperAdmin.builder()
                .username(grsUserDTO.getUsername())
                .password(bCryptPasswordEncoder.encode(grsUserDTO.getPassword()))
                .role(grsRoleDAO.findByRole("OTHERS_COMPLAINANT"))
                .phoneNumber(grsUserDTO.getMobileNumber())
                .email(grsUserDTO.getEmail())
                .officeId(grsUserDTO.getOfficeId())
                .build();

        this.save(superAdmin);


        String smsBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম \n " + superAdmin.getUsername() +
                " এবং পিনকোড  " + pinNumber;
//        String smsBody = "Welcome to GRS. Your Username is \n " + complainant.getUsername() +
//                " and your Pin Number is " + pinNumber;
        String mailBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম " + superAdmin.getUsername() + " এবং পিনকোড ";

        if (superAdmin.getEmail() != null && !superAdmin.getEmail().isEmpty()) {
            emailService.sendEmail(superAdmin.getEmail(),
                    "জিআরএস সিস্টেমে নিবন্ধন",
                    mailBody + pinNumber);
        }
        shortMessageService.sendSMS(superAdmin.getPhoneNumber(), smsBody);



        return true;
    }

    public SuperAdmin findGrsUserByPhoneNumber(String phoneNumber) {
        return this.superAdminDAO.findByPhoneNumber(phoneNumber);
    }

    public List<SuperAdmin> findByRole(long role) {
        return this.superAdminDAO.findByRole(role);
    }

}
