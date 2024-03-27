package com.grs.core.service;

import com.grs.core.dao.GeoDAO;
import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.projapoti.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 02-Oct-17.
 */
@Service
public class GeoService {
    @Autowired
    private GeoDAO geoDao;

    public District getDistrict(Integer districtId) {
        return this.geoDao.getDistrictById(districtId);
    }

    public Division getDivision(Integer divisionId) {
        return this.geoDao.getDivisionById(divisionId);
    }

    public Upazila getUpazilaById(Integer upazilaId) {
        return this.geoDao.getUpazilaById(upazilaId);
    }

    public CityCorporation getCityCorporationById(Integer cityCorporationId) {
        return this.geoDao.getCityCorporationById(cityCorporationId);
    }

    public CountryInfo getNationalityById(Long id) {
        return this.geoDao.getNationalityById(id);
    }

    public List<CountryInfo> getAllNationalitiesAlongWithCountry() {
        return this.geoDao.getAllNationalitiesAlongWithCountry();
    }

    public List<District> getDistricts() {
        return this.geoDao.getDistricts();
    }

    public List<Upazila> getUpazilas(Integer districtId) {
        return this.geoDao.getUpazilas(districtId);
    }

    public List<Division> getDivisions() {
        return this.geoDao.getDivisions();
    }

    public List<District> getDistrictsByDivision(Integer divisionId) {
        return this.geoDao.getDistrictByDivision(divisionId);
    }

    public List<Thana> getThanaByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.geoDao.getThanaByDivisionAndDistrict(divisionId, districtId);
    }

    public List<Upazila> getUpazilaByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.geoDao.getUpazilaByDivisionAndDistrict(divisionId, districtId);
    }

    public List<CityCorporation> getCityCorporationByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.geoDao.getCityCorporationByDivisionAndDistrict(divisionId, districtId);
    }

    public List<Municipality> getMunicipalityByDivisionAndDistrict(Integer divisionId, Integer districtId) {
        return this.geoDao.getMunicipalityByDivisionAndDistrict(divisionId, districtId);
    }

    public Division getDivisionByDistricId(Integer districtId) {
        District district = this.geoDao.getDistrictById(districtId);
        Integer divisionId = district.getDivisionId();
        return this.geoDao.getDivisionById(divisionId);
    }

    public List<CityCorporation> getCityCorporations() {
        return this.geoDao.getCityCorporations();
    }

    public Division getDivisionByCityCorporationId(Integer citycorporationId) {
        CityCorporation cityCorporation = this.geoDao.getCityCorporationById(citycorporationId);
        return this.geoDao.getDivisionById(cityCorporation.getDivisionId());
    }

    public District getDistrictByCityCorporationId(Integer citycorporationId) {
        CityCorporation cityCorporation = this.geoDao.getCityCorporationById(citycorporationId);
        return this.geoDao.getDistrictById(cityCorporation.getDistrictId());
    }
}
