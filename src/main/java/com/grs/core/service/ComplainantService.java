package com.grs.core.service;

import com.google.gson.Gson;
import com.grs.api.model.UserInformation;
import com.grs.api.model.UserType;
import com.grs.api.model.request.BlacklistRequestBodyDTO;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.api.model.request.PasswordChangeDTO;
import com.grs.api.model.response.ComplainantResponseDTO;
import com.grs.api.model.response.UserDetailsDTO;
import com.grs.api.model.response.grievance.ComplainantInfoBlacklistReqDTO;
import com.grs.api.model.response.grievance.ComplainantInfoDTO;
import com.grs.api.model.response.grievance.GrievanceDTO;
import com.grs.api.model.response.grievance.GrievanceShortDTO;
import com.grs.api.myGov.MyGovUser;
import com.grs.core.dao.BlacklistDAO;
import com.grs.core.dao.ComplainantDAO;
import com.grs.core.domain.AddressTypeValue;
import com.grs.core.domain.grs.Blacklist;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.SuperAdmin;
import com.grs.core.domain.projapoti.Office;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.StringUtil;
import com.grs.utils.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Acer on 12-Oct-17.
 */
@Service
public class ComplainantService {
    @Autowired
    private ComplainantDAO complainantDAO;
    @Autowired
    private GrsUserService grsUserService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private GeoService geoService;
    @Autowired
    private OfficesGroService officesGroService;
    @Autowired
    private BlacklistDAO blacklistDAO;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private GrievanceService grievanceService;
    @Autowired
    private MyGovConnectorService myGovConnectorService;

    public Complainant findComplainantByPhoneNumber(String phoneNumber) {
        return this.complainantDAO.findByPhoneNumber(phoneNumber);
    }

    public List<Complainant> findComplainantLikePhoneNumber(String phoneNumber) {
        return this.complainantDAO.findLikePhoneNumber(phoneNumber);
    }

    public Long countAll() {
        return complainantDAO.countAll();
    }

    public Complainant findOne(Long id) {
        return this.complainantDAO.findOne(id);
    }

    public String getRandomPinNumber() {
        SecureRandom random = new SecureRandom();
        Integer num = random.nextInt(1000000);
        String formatted = String.format("%06d", num);
        return formatted;
    }

    public String getRandomPinNumberEightDigit() {
        SecureRandom random = new SecureRandom();
        Integer num = random.nextInt(100000000);
        String formatted = String.format("%06d", num);
        return formatted;
    }

    public Complainant insertComplainant(ComplainantDTO complainantDTO) {
        String pinNumber = getRandomPinNumber();
        complainantDTO.setPinNumber(pinNumber);
        if (this.complainantDAO.findByPhoneNumber(complainantDTO.getPhoneNumber()) != null) {
            return null;
        }
        Complainant complainant = this.complainantDAO.insertComplainant(complainantDTO);
        String smsBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম \n " + complainant.getPhoneNumber() +
                " এবং পিনকোড  " + pinNumber;
//        String smsBody = "Welcome to GRS. Your Username is \n " + complainant.getPhoneNumber() +
//                " and your Pin Number is " + pinNumber;
        String mailBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম " + complainant.getPhoneNumber() + " এবং পিনকোড ";

        if (complainant.getEmail() != null && !complainant.getEmail().isEmpty()) {
            emailService.sendEmail(complainant.getEmail(),
                    "জিআরএস সিস্টেমে নিবন্ধন",
                    mailBody + pinNumber);
        }
        shortMessageService.sendSMS(complainant.getPhoneNumber(), smsBody);
        return complainant;
    }

    public Complainant insertComplainantWithoutLogin(ComplainantDTO complainantDTO) throws Exception {

        Complainant complainant = this.complainantDAO.findByPhoneNumber(complainantDTO.getPhoneNumber());


        if (complainant != null) {
            this.complainantDAO.updateComplainantFromMyGov(complainantDTO, complainant);
            return complainant;
        }

        boolean userExists = true;


        String pinNumber = getRandomPinNumberEightDigit();
        complainantDTO.setPinNumber(pinNumber);

        if (!userExists) {

            String nid = complainantDTO.getIdentificationValue() == null ? null : complainantDTO.getIdentificationValue();
            try {
                complainantDTO = this.myGovConnectorService.createUser(complainantDTO);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            complainantDTO.setIdentificationValue(nid == null ? null : nid);
            complainantDTO.setPinNumber(pinNumber);

            String smsBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম \n " + complainantDTO.getPhoneNumber() +
                    " এবং পিনকোড  " + pinNumber;
            String mailBody = "জিআরএস এ আপনাকে স্বাগতম। আপনার ইউজারনেম " + complainantDTO.getPhoneNumber() + " এবং পিনকোড ";

            if (complainantDTO.getEmail() != null && !complainantDTO.getEmail().isEmpty()) {
                emailService.sendEmail(complainantDTO.getEmail(),
                        "জিআরএস সিস্টেমে নিবন্ধন",
                        mailBody + pinNumber);
            }
            shortMessageService.sendSMS(complainantDTO.getPhoneNumber(), smsBody);
        }

        complainant = this.complainantDAO.insertComplainant(complainantDTO);

        return complainant;
    }


    public ComplainantInfoDTO getComplainantInfo(Long id) {
        Complainant complainant = this.findOne(id);
        ComplainantInfoDTO complainantInfoDTO = this.convertToComplainantDTOForDetailsView(complainant);
        complainantInfoDTO.setId(id);
        return complainantInfoDTO;
    }

    public ComplainantInfoDTO convertToComplainantDTOForDetailsView(Complainant complainant) {
        if (complainant == null) {
            return ComplainantInfoDTO.builder().build();
        }
        ComplainantInfoDTO complainantInfoDTO = ComplainantInfoDTO.builder()
                .name(complainant.getName())
                .mobileNumber(BanglaConverter.convertToBanglaDigit(complainant.getPhoneNumber()))
                .nationalId(BanglaConverter.convertToBanglaDigit(complainant.getIdentificationValue()))
                .email(complainant.getEmail())
                .presentAddress(complainant.getPresentAddressStreet() + (StringUtil.isValidString(complainant.getPresentAddressStreet()) && StringUtil.isValidString(complainant.getPresentAddressHouse()) ? ", " : "") + complainant.getPresentAddressHouse())
                .permanentAddress(complainant.getPermanentAddressStreet() + (StringUtil.isValidString(complainant.getPermanentAddressStreet()) && StringUtil.isValidString(complainant.getPermanentAddressHouse()) ? ", " : "") + complainant.getPermanentAddressHouse())
                .occupation(complainant.getOccupation())
                .dateOfBirth(BanglaConverter.getDateBanglaFromEnglish(DateTimeConverter.convertDateToString(complainant.getBirthDate())))
                .guardianName("")
                .motherName("")
                .build();
        return complainantInfoDTO;
    }

    public String getAddressTypeIdName(Integer addressTypeId, AddressTypeValue addressTypeValue) {
        String endPointName = "";
        if (addressTypeValue == null) {
            return endPointName;
        }
        switch (addressTypeValue) {
            case UPAZILA:
                endPointName = this.geoService.getUpazilaById(addressTypeId).getNameBangla();
                break;
            case CITY_CORPORATION:
                endPointName = this.geoService.getCityCorporationById(addressTypeId).getNameBangla();
                break;
        }
        return endPointName;
    }

    public ComplainantResponseDTO convertToComplainantResponseDTO(Complainant complainant) {
        String permanentAddressTypeIdName = this.getAddressTypeIdName(complainant.getPermanentAddressTypeId(), complainant.getPermanentAddressTypeValue());
        String presentAddressTypeIdName = this.getAddressTypeIdName(complainant.getPresentAddressTypeId(), complainant.getPresentAddressTypeValue());
        return ComplainantResponseDTO.builder()
                .birthDate(complainant.getBirthDate() == null ? null : DateTimeConverter.convertDateToStringForTimeline(complainant.getBirthDate()))
                .education(complainant.getEducation())
                .gender(complainant.getGender())
                .name(complainant.getName())
                .nationality(complainant.getCountryInfo().getNationalityBng())
                .email(complainant.getEmail())
                .nidOrBcn(complainant.getIdentificationValue())
                /*.permanentAddressDistrictId(complainant.getPermanentAddressDistrictId())
                .permanentAddressDistrictNameBng(complainant.getPermanentAddressDistrictNameBng())
                .permanentAddressDistrictNameEng(complainant.getPermanentAddressDistrictNameEng())
                .permanentAddressDivisionId(complainant.getPermanentAddressDivisionId())
                .permanentAddressDivisionNameEng(complainant.getPermanentAddressDivisionNameEng())
                .permanentAddressDivisionNameBng(complainant.getPermanentAddressDivisionNameBng())
                */
                .permanentAddressStreet(complainant.getPermanentAddressStreet())
                .permanentAddressHouse(complainant.getPermanentAddressHouse())
                /*.permanentAddressTypeId(complainant.getPermanentAddressTypeId())
                .permanentAddressTypeNameBng(complainant.getPermanentAddressTypeNameBng())
                .permanentAddressTypeNameEng(complainant.getPermanentAddressTypeNameEng())
                .permanentAddressTypeValue(complainant.getPermanentAddressTypeValue())
                .presentAddressDistrictId(complainant.getPresentAddressDistrictId())
                .presentAddressDistrictNameBng(complainant.getPresentAddressDistrictNameBng())
                .presentAddressDistrictNameEng(complainant.getPresentAddressDistrictNameEng())
                .presentAddressDivisionId(complainant.getPresentAddressDivisionId())
                .presentAddressDivisionNameBng(complainant.getPresentAddressDivisionNameBng())
                .presentAddressDivisionNameEng(complainant.getPresentAddressDivisionNameEng())
                .presentAddressStreet(complainant.getPresentAddressStreet())
                .presentAddressHouse(complainant.getPresentAddressHouse())
                .presentAddressTypeId(complainant.getPresentAddressTypeId())
                .presentAddressTypeNameBng(complainant.getPresentAddressTypeNameBng())
                .presentAddressTypeNameEng(complainant.getPresentAddressTypeNameEng())
                .presentAddressTypeValue(complainant.getPresentAddressTypeValue())*/
                .permanentAddressCountryId(complainant.getPermanentAddressCountryId())
                .presentAddressCountryId(complainant.getPresentAddressCountryId())
                /*.permanentAddressCountryName(this.geoService.getNationalityById(complainant.getPermanentAddressCountryId()).getCountryNameBng())
                .presentAddressCountryName(this.geoService.getNationalityById(complainant.getPresentAddressCountryId()).getCountryNameBng())*/
                .foreignPermanentAddressLine1(complainant.getForeignPermanentAddressLine1())
                .foreignPermanentAddressLine2(complainant.getForeignPermanentAddressLine2())
                /*.foreignPermanentAddressCity(complainant.getForeignPermanentAddressCity())
                .foreignPermanentAddressZipCode(complainant.getForeignPermanentAddressZipCode())
                .foreignPermanentAddressState(complainant.getForeignPermanentAddressState())
                .foreignPresentAddressLine1(complainant.getForeignPresentAddressLine1())
                .foreignPresentAddressLine2(complainant.getForeignPresentAddressLine2())
                .foreignPresentAddressCity(complainant.getForeignPresentAddressCity())
                .foreignPresentAddressZipCode(complainant.getForeignPresentAddressZipCode())
                .foreignPresentAddressState(complainant.getForeignPresentAddressState())*/
                .education(complainant.getEducation())
                .occupation(complainant.getOccupation())
                .phoneNumber(complainant.getPhoneNumber())
                .build();
    }

    public Complainant save(Complainant complainant) {
        return this.complainantDAO.save(complainant);
    }

    public boolean doBlacklistByComplainantId(Long complainantId, Long officeId) {
        Boolean flag = this.blacklistDAO.doBlacklistByComplainantId(complainantId, officeId);
        if (flag) {
            ComplainantInfoDTO complainant = getComplainantInfo(complainantId);
            shortMessageService.sendSMS(complainant.getMobileNumber(), "Dear " + complainant.getName() + ", you have been Blacklisted at GRS");
            emailService.sendEmail(complainant.getEmail(), "Blacklisted in GRS", "Dear  " + complainant.getName() + ", \n\nYou have been Blacklisted from GRS. Therefore you will not be able to access several functionality in grs.gov.bd\n");
        }
        return flag;
    }

    public boolean doBlacklistRequestByComplainantId(BlacklistRequestBodyDTO blacklistRequestBodyDTO, UserInformation userInformation) {
        Boolean flag = this.blacklistDAO.doBlacklistRequestByComplainantId(blacklistRequestBodyDTO, userInformation);
        //change this sms sending part.
        if (flag) {
            ComplainantInfoDTO complainant = getComplainantInfo(blacklistRequestBodyDTO.getComplainantId());
            shortMessageService.sendSMS(complainant.getMobileNumber(), "Dear " + complainant.getName() + ", you have been Blacklisted at GRS");
            emailService.sendEmail(complainant.getEmail(), "Blacklisted in GRS", "Dear  " + complainant.getName() + ", \n\nYou have been Blacklisted from GRS. Therefore you will not be able to access several functionality in grs.gov.bd\n");
        }
        return flag;
    }

    public boolean doBlacklistRequestByComplainantId(Long complainantId, UserInformation userInformation) {
        return this.blacklistDAO.doBlacklistRequestByComplainantId(complainantId, userInformation.getOfficeInformation().getOfficeId());
    }

    public Boolean isBlacklistedUser(Authentication authentication) {
        UserInformation userInformation = Utility.extractUserInformationFromAuthentication(authentication);
        if (userInformation.getUserType().equals(UserType.COMPLAINANT)) {
            return isBlacklistedUserByComplainantId(userInformation.getUserId());
        }
        return false;
    }

    public Boolean isBlacklistedUserByComplainantId(Long complainantId) {
        return this.blacklistDAO.isBlacklistedUserByComplainantId(complainantId);
    }

    public List<ComplainantInfoDTO> getBlacklistByOfficeId(Long officeId) {
        return this.blacklistDAO.getBlacklistByOfficeId(officeId)
                .stream()
                .map(x -> {
                    Complainant complainant = this.complainantDAO.findOne(x.getComplainantId());
                    return ComplainantInfoDTO.builder()
                            .email(complainant.getEmail())
                            .id(x.getComplainantId())
                            .mobileNumber(complainant.getPhoneNumber())
                            .name(complainant.getName())
                            .occupation(complainant.getOccupation())
                            .isBlacklisted(x.getBlacklisted())
                            .isRequested(x.getRequested())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ComplainantInfoBlacklistReqDTO> getBlacklistRequestByChildOffices(Long officeId, Long officeUnitOrganogramId) {

        List<Office> offices = officesGroService.findByAppealOfficer(officeId, officeUnitOrganogramId);
        return this.blacklistDAO.getBlacklistByChildOfficesAndIsRequested(offices.stream().map(Office::getId).collect(Collectors.toList()))
                .stream()
                .map(x -> {
                    Complainant complainant = this.complainantDAO.findOne(x.getComplainantId());
                    return ComplainantInfoBlacklistReqDTO.builder()
                            .id(x.getComplainantId())
                            .mobileNumber(complainant.getPhoneNumber())
                            .name(complainant.getName())
                            .occupation(complainant.getOccupation())
                            .isBlacklisted(x.getBlacklisted())
                            .isRequested(x.getRequested())
                            .officeId(x.getOfficeId())
                            .officeName(x.getOfficeName())
                            .blacklistReason(x.getReason())
                            .build();
                })
                .collect(Collectors.toList());
    }


    public boolean doUnBlacklistByComplainantId(Long complainantId, Long officeId) {
        Boolean flag = this.blacklistDAO.doUnBlacklistByComplainantId(complainantId, officeId);
        if (flag) {
            ComplainantInfoDTO complainant = getComplainantInfo(complainantId);
            shortMessageService.sendSMS(complainant.getMobileNumber(), "Dear " + complainant.getName() + ", You have been removed from Black List in GRS");
            emailService.sendEmail(complainant.getEmail(), "Escaped from Blacklist in GRS", "Dear " + complainant.getName() + ",\n\nYou were Blacklisted from GRS but you have been removed from the list. Therefore you will be able to access several functionality in grs.gov.bd from which you were restricted to.\n");
        }
        return flag;
    }

    public boolean doUnBlacklistRequestByComplainantId(Long complainantId, Long officeId) {
        Boolean flag = this.blacklistDAO.doUnBlacklistRequestByComplainantId(complainantId, officeId);
        if (flag) {
            ComplainantInfoDTO complainant = getComplainantInfo(complainantId);
            shortMessageService.sendSMS(complainant.getMobileNumber(), "Dear " + complainant.getName() + ", You have been removed from Black List in GRS");
            emailService.sendEmail(complainant.getEmail(), "Escaped from Blacklist in GRS", "Dear " + complainant.getName() + ",\n\nYou were Blacklisted from GRS but you have been removed from the list. Therefore you will be able to access several functionality in grs.gov.bd from which you were restricted to.\n");
        }
        return flag;
    }

    public ComplainantDTO getComplaintDTO(UserInformation userInformation) {
        return this.getComplaintDTO(userInformation.getUserId());
    }

    public ComplainantDTO getComplaintDTO(Long id) {
        Complainant complainant = this.complainantDAO.findOne(id);
        return this.complainantDAO.convertToComplainantDTO(complainant);
    }

    public Complainant updateComplainant(UserInformation userInformation, ComplainantDTO complainantDTO) {
        Complainant complainant = this.complainantDAO.findOne(userInformation.getUserId());
        return this.complainantDAO.updateComplainant(complainantDTO, complainant);
    }

    public List<Long> findBlacklistedOffices(Long complainantId) {
        return this.blacklistDAO.findByComplainantId(complainantId).stream().map(Blacklist::getOfficeId).collect(Collectors.toList());
    }

    public Boolean updateComplainantPassword(UserInformation userInformation, PasswordChangeDTO passwordChangeDTO) {
        Complainant complainant = this.complainantDAO.findOne(userInformation.getUserId());
        if (bCryptPasswordEncoder.matches(passwordChangeDTO.getOldPassword(), complainant.getPassword())) {
            complainant.setPassword(bCryptPasswordEncoder.encode(passwordChangeDTO.getNewPassword()));
            this.complainantDAO.save(complainant);
            return true;
        }
        return false;
    }

    public Page<UserDetailsDTO> getPaginatedUsersData(Pageable pageable) {
        List<Complainant> complainants = this.complainantDAO.findAll();
        List<SuperAdmin> grsUsers = this.grsUserService.findByRole(10);
        List<UserDetailsDTO> complainantData = complainants.stream().map(complainant -> {
            List<GrievanceDTO> grievances = this.grievanceService.getGrievancesByComplainantId(complainant.getId());
            List<GrievanceShortDTO> shortDTOS = new ArrayList<>();
            if (grievances != null && grievances.size() > 0) {
                grievances.forEach(
                        grievanceDTO -> {
                            shortDTOS.add(GrievanceShortDTO.builder()
                                    .grievanceId(grievanceDTO.getId())
                                    .subject(grievanceDTO.getSubject())
                                    .currentStatus(grievanceDTO.getStatusBangla())
                                    .build());
                        }
                );
            }
            return UserDetailsDTO.builder()
                    .username(complainant.getName())
                    .email(complainant.getEmail())
                    .phone(complainant.getPhoneNumber())
                    .grievances(shortDTOS)
                    .build();
        }).collect(Collectors.toList());

        List<UserDetailsDTO> grsUserData = grsUsers.stream().map(complainant -> {
            List<GrievanceDTO> grievances = this.grievanceService.findGrievancesByOthersComplainant(complainant.getId());
            List<GrievanceShortDTO> shortDTOS = new ArrayList<>();
            if (grievances != null && grievances.size() > 0) {
                grievances.forEach(
                        grievanceDTO -> {
                            shortDTOS.add(GrievanceShortDTO.builder()
                                    .grievanceId(grievanceDTO.getId())
                                    .subject(grievanceDTO.getSubject())
                                    .currentStatus(grievanceDTO.getStatusBangla())
                                    .build());
                        }
                );
            }
            return UserDetailsDTO.builder()
                    .username(complainant.getUsername())
                    .email(complainant.getEmail())
                    .phone(complainant.getPhoneNumber())
                    .grievances(shortDTOS)
                    .build();
        }).collect(Collectors.toList());

        complainantData.addAll(grsUserData);

//        complainantData.getContent().addAll(grsUserData.getContent());
        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), complainantData.size());
        final Page<UserDetailsDTO> page = new PageImpl<>(complainantData.subList(start, end), pageable, complainantData.size());
        return page;
    }
}
