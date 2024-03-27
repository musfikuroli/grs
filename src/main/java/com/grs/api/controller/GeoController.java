package com.grs.api.controller;

import com.grs.core.domain.grs.CountryInfo;
import com.grs.core.domain.projapoti.*;
import com.grs.core.service.GeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Acer on 02-Oct-17.
 */
@RestController
public class GeoController {
    @Autowired
    private GeoService geoService;

    @RequestMapping(value = "/api/geo/countries", method = RequestMethod.GET)
    public List<CountryInfo>  getAllNationalitiesAlongWithCountry() {
        return this.geoService.getAllNationalitiesAlongWithCountry();
    }

    @RequestMapping(value = "/api/geo/districts", method = RequestMethod.GET)
    public List<District> getDistricts() {
        return this.geoService.getDistricts();
    }

    @RequestMapping(value = "/api/geo/division/{districtId}", method = RequestMethod.GET)
    public Division getDivisionByDistricId(@PathVariable("districtId") Integer districtId) {
        return this.geoService.getDivisionByDistricId(districtId);
    }

    @RequestMapping(value = "/api/geo/upazilas/{districtId}", method = RequestMethod.GET)
    public List<Upazila> getUpazilas(@PathVariable("districtId") Integer districtId) {
        return this.geoService.getUpazilas(districtId);
    }

    @RequestMapping( value = "/api/geo/division", method = RequestMethod.GET)
    public List<Division> getDivisions() {
        return this.geoService.getDivisions();
    }

    @RequestMapping(value = "/api/geo/district/{divisionId}", method = RequestMethod.GET)
    public List<District> getDistrictsByDivision(@PathVariable("divisionId") Integer divisionId) {
        return this.geoService.getDistrictsByDivision(divisionId);
    }

    @RequestMapping(value = "/api/geo/thana/{divisionId}/{districtId}", method = RequestMethod.GET)
    public List<Thana> getThanaByDivisionAndDistrict(@PathVariable("divisionId") Integer divisionId, @PathVariable("districtId") Integer districtId) {
        return this.geoService.getThanaByDivisionAndDistrict(divisionId, districtId);
    }

    @RequestMapping(value = "/api/geo/upazila/{divisionId}/{districtId}", method = RequestMethod.GET)
    public List<Upazila> getUpazilaByDivisionAndDistrict(@PathVariable("divisionId") Integer divisionId, @PathVariable("districtId") Integer districtId) {
        return this.geoService.getUpazilaByDivisionAndDistrict(divisionId, districtId);
    }

    @RequestMapping(value = "/api/geo/citycorporation/{divisionId}/{districtId}", method = RequestMethod.GET)
    public List<CityCorporation> getCityCorporationByDivisionAndDistrict(@PathVariable("divisionId") Integer divisionId, @PathVariable("districtId") Integer districtId) {
        return this.geoService.getCityCorporationByDivisionAndDistrict(divisionId, districtId);
    }

    @RequestMapping(value = "/api/geo/municipality/{divisionId}/{districtId}", method = RequestMethod.GET)
    public List<Municipality> getMunicipalityByDivisionAndDistrict(@PathVariable("divisionId") Integer divisionId, @PathVariable("districtId") Integer districtId) {
        return this.geoService.getMunicipalityByDivisionAndDistrict(divisionId, districtId);
    }

    @RequestMapping(value = "/api/geo/citycorporations", method = RequestMethod.GET)
    public List<CityCorporation> getCityCorporations() {
        return this.geoService.getCityCorporations();
    }

    @RequestMapping(value = "/api/geo/citycorporation/division/{citycorporationId}", method = RequestMethod.GET)
    public Division getDivisionByCityCorporationId(@PathVariable("citycorporationId") Integer citycorporationId) {
        return this.geoService.getDivisionByCityCorporationId(citycorporationId);
    }

    @RequestMapping(value = "/api/geo/citycorporation/district/{citycorporationId}", method = RequestMethod.GET)
    public District getDistrictByCityCorporationId(@PathVariable("citycorporationId") Integer citycorporationId) {
        return this.geoService.getDistrictByCityCorporationId(citycorporationId);
    }
}
