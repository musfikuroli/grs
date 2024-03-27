package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.EmployeeOffice;
import com.grs.core.domain.projapoti.EmployeeRecord;
import com.grs.core.domain.projapoti.Office;
import com.grs.core.domain.projapoti.OfficeUnitOrganogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Tanvir on 9/7/2017.
 */
@Repository
public interface EmployeeOfficeRepo extends JpaRepository<EmployeeOffice, Long> {

    public List<EmployeeOffice> findByStatusAndOfficeUnitOrganogram(Boolean status, OfficeUnitOrganogram officeUnitOrganogram);

    public List<EmployeeOffice> findByOfficeAndOfficeUnitOrganogramAndStatus(Office office, OfficeUnitOrganogram officeUnitOrganogram, boolean status);

    public EmployeeOffice findByEmployeeRecord(EmployeeRecord employeeRecord);

    public EmployeeOffice findByOfficeAndEmployeeRecordAndStatus(Office office, EmployeeRecord employeeRecord, Boolean status);

    public List<EmployeeOffice> findByEmployeeRecordIdAndStatus(Long employeeRecordId, Boolean status);

    public List<EmployeeOffice> findEmployeeOfficeByOfficeIdAndIsOfficeHeadAndStatus(Long officeId, Boolean isOfficeHead, Boolean status);

    //EmployeeOffice findByOfficeIdAndOfficeUnitOrganogramId(Long officeId, Long officeUnitOrganogramId);

    @Query(
            nativeQuery = true,
            value = "select * \n" +
                    "from employee_offices\n" +
                    "where office_unit_organogram_id=:officeUnitOrgId\n" +
                    "and status=true\n")
    EmployeeOffice findByOfficeUnitOrganogramIdAndStatus(@Param("officeUnitOrgId") Long officeUnitOrgId);

    @Query(
            nativeQuery = true,
            value = "select * \n" +
                    "from employee_offices\n" +
                    "where office_id=:officeId and office_unit_organogram_id=:officeUnitOrgId\n" +
                    "and status=true\n")
    EmployeeOffice findByOfficeIdAndOfficeUnitOrganogramId(@Param("officeId") Long officeId,@Param("officeUnitOrgId") Long officeUnitOrgId);

    @Query(
            nativeQuery = true,
            value = "select * \n" +
                    "from employee_offices\n" +
                    "where office_id=:officeId and office_unit_organogram_id=:officeUnitOrgId")
    List<EmployeeOffice> findByOfficeIdAndOfficeUnitOrganogramIdWIthoutStatus(@Param("officeId") Long officeId,@Param("officeUnitOrgId") Long officeUnitOrgId);

    List<EmployeeOffice> findByOfficeIdAndOfficeUnitOrganogramIdAndStatus(Long officeId, Long officeUnitOrganogramId, Boolean status);

    List<EmployeeOffice> findByOfficeAndStatus(Office office, Boolean status);
}
