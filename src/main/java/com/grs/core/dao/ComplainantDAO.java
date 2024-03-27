package com.grs.core.dao;


import com.grs.api.model.UserInformation;
import com.grs.api.model.request.BlacklistRequestBodyDTO;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.core.domain.*;
import com.grs.core.domain.grs.Complainant;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.Gender;
import com.grs.core.repo.grs.ComplainantRepo;
import com.grs.utils.BanglaConverter;
import com.grs.utils.DateTimeConverter;
import com.grs.utils.StringUtil;
import org.reflections.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/27/2017.
 */
@Service
public class ComplainantDAO {
    @Autowired
    private ComplainantRepo complainantRepo;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private GeoDAO geoDAO;

    public Long countAll() {return this.complainantRepo.count();}

    public Complainant findOne(Long id) {
        return this.complainantRepo.findOne(id);
    }

    public Complainant findByUsername(String username) {
        return this.complainantRepo.findByUsername(username);
    }

    public Complainant findByUsernameAndPassword(String username, String password) {
        return this.complainantRepo.findByUsernameAndPassword(username, password);
    }

    public Complainant findByPhoneNumber(String phoneNumber) {
        return this.complainantRepo.findByPhoneNumber(BanglaConverter.convertAllToEnglish(phoneNumber));
    }

    public List<Complainant> findLikePhoneNumber(String phoneNumber) {
        return this.complainantRepo.findByPhoneNumberIsContaining(BanglaConverter.convertAllToEnglish(phoneNumber));
    }

    public Complainant insertComplainant(ComplainantDTO complainantDTO) {
        Complainant complainant = this.convertToComplaint(complainantDTO);
        return this.complainantRepo.save(complainant);
    }

    public Complainant convertToComplaint(ComplainantDTO complainantDTO) {
        CountryInfo countryInfo = Utils.isEmpty(complainantDTO.getNationality()) ? this.geoDAO.getNationalityById(Long.valueOf(15)) : this.geoDAO.getNationalityById(Long.valueOf(complainantDTO.getNationality()));
        String birthDate = complainantDTO.getBirthDate();
        Complainant complainant = Complainant.builder()
                .name(complainantDTO.getName())
                .email(complainantDTO.getEmail())
                .permanentAddressCountryId(complainantDTO.getPermanentAddressCountryId() == null ? null : Long.valueOf(complainantDTO.getPermanentAddressCountryId()))
                .countryInfo(countryInfo)
                .education(complainantDTO.getEducation())
                .identificationType(IdentificationType.valueOf(complainantDTO.getIdentificationType()))
                .identificationValue(complainantDTO.getIdentificationValue())
                .occupation(complainantDTO.getOccupation())
                .phoneNumber(BanglaConverter.convertToEnglish(complainantDTO.getPhoneNumber()))
                .gender(complainantDTO.getGender() == null ? null : Gender.valueOf(complainantDTO.getGender()))
                .permanentAddressStreet(complainantDTO.getPermanentAddressStreet())
                .permanentAddressHouse(complainantDTO.getPermanentAddressHouse())
                .foreignPermanentAddressLine1(complainantDTO.getForeignPermanentAddressLine1())
                .foreignPermanentAddressLine2(complainantDTO.getForeignPermanentAddressLine2())
                .username(BanglaConverter.convertToEnglish(complainantDTO.getPhoneNumber()))
                .password( complainantDTO.getPinNumber() != null ? bCryptPasswordEncoder.encode(complainantDTO.getPinNumber()) : null)
                .birthDate((birthDate == null || birthDate.isEmpty()) ? null : DateTimeConverter.convertToDate(complainantDTO.getBirthDate()))
                .authenticated(true)
                .build();
        return complainant;
    }

    public ComplainantDTO convertToComplainantDTO(Complainant complainant) {
        String birthDate = complainant.getBirthDate() == null ? "" : DateTimeConverter.convertDateToStringForTimeline(complainant.getBirthDate());
        String nationality = complainant.getCountryInfo() == null ? "15" : complainant.getCountryInfo().getId().toString();

        ComplainantDTO complainantDTO = ComplainantDTO.builder()
                .name(complainant.getName())
                .email(complainant.getEmail())
                .nationality(nationality)
                .permanentAddressCountryId(complainant.getPermanentAddressCountryId() == null ? null : String.valueOf(complainant.getPermanentAddressCountryId()))
                .education(complainant.getEducation())
                .identificationType(complainant.getIdentificationType().name())
                .identificationValue(complainant.getIdentificationValue())
                .occupation(complainant.getOccupation())
                .phoneNumber(BanglaConverter.convertToEnglish(complainant.getPhoneNumber()))
                .gender(complainant.getGender() == null ? "" : complainant.getGender().name())

                .permanentAddressStreet(complainant.getPermanentAddressStreet())
                .permanentAddressHouse(complainant.getPermanentAddressHouse())
                .foreignPermanentAddressLine1(complainant.getForeignPermanentAddressLine1())
                .foreignPermanentAddressLine2(complainant.getForeignPermanentAddressLine2())
                .birthDate(birthDate)
                .build();

        return complainantDTO;
    }

    public Complainant save(Complainant complainant) {
        CountryInfo countryInfo = complainant.getCountryInfo() == null ? this.geoDAO.getNationalityById(Long.valueOf(15)) : complainant.getCountryInfo();
        complainant.setCountryInfo(countryInfo);
        return this.complainantRepo.save(complainant);
    }

    public Complainant updateComplainant(ComplainantDTO complainantDTO, Complainant newComplainant){
        Complainant complainant = this.convertToComplaint(complainantDTO);
        newComplainant.setName(complainant.getName());
        newComplainant.setEmail(complainant.getEmail());
        newComplainant.setIdentificationValue(complainant.getIdentificationValue());
        newComplainant.setIdentificationType(complainant.getIdentificationType());
        newComplainant.setGender(complainant.getGender());
        newComplainant.setCountryInfo(complainant.getCountryInfo());
        newComplainant.setOccupation(complainant.getOccupation());
        newComplainant.setEducation(complainant.getEducation());

        newComplainant.setPermanentAddressStreet(complainant.getPermanentAddressStreet());
        newComplainant.setPermanentAddressHouse(complainant.getPermanentAddressHouse());

        newComplainant.setBirthDate(complainant.getBirthDate());
        if (StringUtil.isValidString(complainant.getPhoneNumber())) {
            newComplainant.setPhoneNumber(BanglaConverter.convertAllToEnglish(complainant.getPhoneNumber()));
            newComplainant.setUsername(BanglaConverter.convertAllToEnglish(complainant.getPhoneNumber()));
        }

        newComplainant.setForeignPermanentAddressLine1(complainant.getForeignPermanentAddressLine1());
        newComplainant.setForeignPermanentAddressLine2(complainant.getForeignPermanentAddressLine2());

        return this.complainantRepo.save(newComplainant);
    }

    public Complainant updateComplainantFromMyGov(ComplainantDTO complainantDTO, Complainant existingComplainant){
        Complainant complainant = this.convertToComplaint(complainantDTO);
        if (complainant.getName() != null && !complainant.getName().isEmpty()) existingComplainant.setName(complainant.getName());
        if (complainant.getEmail() != null && !complainant.getEmail().isEmpty()) existingComplainant.setEmail(complainant.getEmail());
        if (complainant.getIdentificationValue() != null && !complainant.getIdentificationValue().isEmpty()) existingComplainant.setIdentificationValue(complainant.getIdentificationValue());
        if (complainant.getIdentificationType() != null) existingComplainant.setIdentificationType(complainant.getIdentificationType());
        if (complainant.getGender() != null) existingComplainant.setGender(complainant.getGender());
        if (complainant.getCountryInfo() != null) existingComplainant.setCountryInfo(complainant.getCountryInfo());
        if (complainant.getOccupation() != null && !complainant.getOccupation().isEmpty()) existingComplainant.setOccupation(complainant.getOccupation());
        if (complainant.getEducation() != null && !complainant.getEducation().isEmpty()) existingComplainant.setEducation(complainant.getEducation());
        if (complainant.getPermanentAddressStreet() != null && !complainant.getPermanentAddressStreet().isEmpty()) existingComplainant.setPermanentAddressStreet(complainant.getPermanentAddressStreet());
        if (complainant.getPermanentAddressHouse() != null && !complainant.getPermanentAddressHouse().isEmpty()) existingComplainant.setPermanentAddressHouse(complainant.getPermanentAddressHouse());
        if (complainant.getBirthDate() != null) existingComplainant.setBirthDate(complainant.getBirthDate());
        if (StringUtil.isValidString(complainant.getPhoneNumber())) {
            existingComplainant.setPhoneNumber(BanglaConverter.convertAllToEnglish(complainant.getPhoneNumber()));
            existingComplainant.setUsername(BanglaConverter.convertAllToEnglish(complainant.getPhoneNumber()));
        }
        if (complainant.getForeignPermanentAddressLine1() != null && !complainant.getForeignPermanentAddressLine1().isEmpty()) existingComplainant.setForeignPermanentAddressLine1(complainant.getForeignPermanentAddressLine1());
        if (complainant.getForeignPermanentAddressLine2() != null && !complainant.getForeignPermanentAddressLine2().isEmpty()) existingComplainant.setForeignPermanentAddressLine2(complainant.getForeignPermanentAddressLine2());

        return this.complainantRepo.save(existingComplainant);
    }

    public List<Complainant> findAll(){
        return this.complainantRepo.findAll();
    }
}
