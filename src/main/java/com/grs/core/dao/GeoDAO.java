package com.grs.core.dao;

import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.repo.grs.NationalityAndCountryRepo;
import com.grs.core.repo.projapoti.*;
import com.grs.core.domain.projapoti.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 02-Oct-17.
 */
@Service
public class GeoDAO {
    @Autowired
    private DistrictRepo districtRepo;

    @Autowired
    private UpazilaRepo upazilaRepo;

    @Autowired
    private DivisionRepo divisionRepo;

    @Autowired
    private ThanaRepo thanaRepo;

    @Autowired
    private CityCorporationRepo cityCorporationRepo;

    @Autowired
    private MunicipalityRepo municipalityRepo;

    @Autowired
    private NationalityAndCountryRepo nationalityAndCountryRepo;

    public CountryInfo getNationalityById(Long id) {
        return this.nationalityAndCountryRepo.findOne(id);
    }

    public List<CountryInfo> getAllNationalitiesAlongWithCountry() {
        return this.nationalityAndCountryRepo.findAll();
    }

    public  List<District> getDistricts() {
        return this.districtRepo.findAll();
    }

    public List<Upazila> getUpazilas(Integer districtId) {
        return this.upazilaRepo.findByDistrictId(districtId);
    }

    public List<Division> getDivisions() {
        return this.divisionRepo.findAll();
    }

    public List<District> getDistrictByDivision(Integer divisionId){
        return this.districtRepo.findByDivisionId(divisionId);
    }
    public List<Thana> getThanaByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.thanaRepo.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public List<Upazila> getUpazilaByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.upazilaRepo.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public List<CityCorporation> getCityCorporationByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.cityCorporationRepo.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public List<Municipality> getMunicipalityByDivisionAndDistrict(Integer divisionId, Integer districtId){
        return this.municipalityRepo.findByDivisionIdAndDistrictId(divisionId, districtId);
    }

    public District getDistrictById(Integer districtId) {
        return this.districtRepo.findOne(districtId);
    }

    public Division getDivisionById(Integer divisionId) {
        return this.divisionRepo.findOne(divisionId);
    }

    public List<CityCorporation> getCityCorporations() {
        return this.cityCorporationRepo.findAll();
    }

    public CityCorporation getCityCorporationById(Integer citycorporationId) {
        return this.cityCorporationRepo.findOne(citycorporationId);
    }

    public Upazila getUpazilaById(Integer upazilaId) {
        return this.upazilaRepo.findOne(upazilaId);
    }
}
