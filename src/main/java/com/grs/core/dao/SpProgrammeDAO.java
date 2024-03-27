package com.grs.core.dao;

import com.grs.api.model.OfficeInformationFullDetails;
import com.grs.api.model.response.GroContactInfoResponseDTO;
import com.grs.api.model.response.SpProgramDto;
import com.grs.api.model.response.SpProgramGroDto;
import com.grs.core.domain.grs.Occupation;
import com.grs.core.domain.grs.OfficesGRO;
import com.grs.core.domain.grs.SpProgramme;
import com.grs.core.domain.projapoti.EmployeeOffice;
import com.grs.core.domain.projapoti.EmployeeRecord;
import com.grs.core.domain.projapoti.OfficeUnit;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import com.grs.core.service.OfficeService;
import com.grs.core.service.OfficesGroService;
import com.grs.utils.Constant;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.*;

@Service
public class SpProgrammeDAO {

    @Autowired
    EntityManager entityManager;

    @Autowired
    OfficeService officeService;

    @Autowired
    private com.grs.core.repo.grs.SpProgrammeRepo spProgrammeRepo;

    @Autowired
    private OfficesGroService officesGroService;

    public List<com.grs.core.domain.grs.SpProgramme> findAll() {
        return this.spProgrammeRepo.findAll();
    }

    public List<SpProgramme> findAllByStatusAndOfficeIdNotNull() {
        return this.spProgrammeRepo.findAllByStatus(true)
                .stream().filter(spProgramme -> spProgramme.getOfficeId() != null).collect(Collectors.toList());
    }

    public SpProgramme findOne(Integer id) {
        return this.spProgrammeRepo.findOne(id);
    }

    public List<SpProgramGroDto> getSpProgramGroDetailsList() {
        try {
//            List<SpProgramme> list = findAll();
            List<SpProgramGroDto> returnList = new ArrayList<>();

            String query = "select spp.id, spp.name_bn, og.office_name_bng, ou.unit_name_bng,\n" +
                    " er.name_bng, eo.designation,\n" +
                    " er.personal_mobile, er.personal_email\n" +
                    " from sp_programme spp\n" +
                    " left join offices_gro og on spp.office_id=og.office_id\n" +
                    " left join grs_doptor.employee_offices eo\n" +
                    " on eo.office_id=og.gro_office_id\n" +
                    " and eo.office_unit_organogram_id=og.gro_office_unit_organogram_id\n" +
                    " left join grs_doptor.office_unit_organograms ouo on ouo.id=og.gro_office_unit_organogram_id\n" +
                    " left join grs_doptor.office_units ou on ouo.office_unit_id=ou.id\n" +
                    " left join grs_doptor.employee_records er\n" +
                    " on er.id=eo.employee_record_id\n" +
                    " where eo.status=true\n" +
                    " or eo.id is null\n" +
                    " group by spp.id";

            List<Object[]> entityList = entityManager.createNativeQuery(query).getResultList();

            Set<Integer> programIds = new HashSet<Integer>();

            if (entityList != null) {
                for (Object[] row : entityList) {
                    if (row[0] != null) {
                        SpProgramGroDto dto = new SpProgramGroDto();
                        dto.setId(Integer.parseInt(row[0]+""));
                        programIds.add(dto.getId());
                        if (row[1] != null) {
                            dto.setSpProgramName(row[1]+"");
                        }
                        if (row[2] != null) {
                            dto.setOfficeName(row[2]+"");
                        }
                        if (row[3] != null) {
                            dto.setOfficeUnitOrganogramName(row[3]+"");
                        }
                        if (row[4] != null) {
                            dto.setOfficeGroName(row[4]+"");
                        }
                        if (row[5] != null) {
                            dto.setOfficeGroDesignation(row[5]+"");
                        }
                        if (row[6] != null) {
                            dto.setOfficeGroPhoneNumber(row[6]+"");
                        }
                        if (row[7] != null) {
                            dto.setOfficeGroEmail(row[7]+"");
                        }
                        returnList.add(dto);
                    }
                }
            }

            List<com.grs.core.domain.grs.SpProgramme> programmeList = findAll();
            for (com.grs.core.domain.grs.SpProgramme program: programmeList) {
                if (programIds != null && !programIds.contains(program.getId())) {
                    SpProgramGroDto dto = new SpProgramGroDto();
                    dto.setId(program.getId());
                    dto.setSpProgramName(program.getNameBn());
                    if (program.getOfficeId() != null) {
                        try {
                            dto.setOfficeName(officesGroService.findOfficesGroByOfficeId(program.getOfficeId()).getOfficeNameBangla());
                        } catch (Exception ex) {
                            System.out.println(ex.toString());
                        }
                    }
                    returnList.add(dto);
                }
            }

            return returnList;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public void saveSpProgram(SpProgramme dto){
        try {
            this.spProgrammeRepo.save(dto);
        } catch (Exception ex) {
            throw ex;
        }
    }

    public Integer checkUniqueSpProgram(SpProgramDto dto) {
        try {
            Integer count = spProgrammeRepo.countByNameEnOrNameBn(dto.getNameEn(), dto.getNameBn());
            return count;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public boolean checkIfGroExistsForSpProgramOffice(Long officeId) {
        try {
            OfficesGRO officesGRO = this.officesGroService.findOfficesGroByOfficeId(officeId);
            if (officesGRO == null) return false;
            Long groOrganogramId = officesGRO.getGroOfficeUnitOrganogramId();
            OfficeUnitOrganogram toOfficeUnitOrganogram;
            toOfficeUnitOrganogram = this.officeService.getOfficeUnitOrganogramById(groOrganogramId);
            if (toOfficeUnitOrganogram == null) return false;

            EmployeeOffice employeeOffice = this.officeService.findEmployeeOfficeByOfficeAndOfficeUnitOrganogramAndStatus(
                    officeId,
                    groOrganogramId,
                    true
            );
            if (employeeOffice == null) return false;

            return true;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
