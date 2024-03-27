package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.GrievanceForwarding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 05-Oct-17.
 */
@Repository
public interface GrievanceForwardingRepo extends JpaRepository<GrievanceForwarding, Long>, JpaSpecificationExecutor<GrievanceForwarding> {
    public List<GrievanceForwarding> findByGrievance(Grievance grievance);

    public GrievanceForwarding findTopByGrievanceOrderByIdDesc(Grievance grievance);

    public GrievanceForwarding findByGrievanceAndToOfficeIdAndToOfficeUnitOrganogramIdAndIsCurrent(Grievance grievance, Long officeId, Long officeUnitOrganogramId, Boolean isCurrent);

    @Query(value = "select * from complaint_movements as cm \n" +
            "where cm.complaint_id=?1 and (cm.`action` like '%CLOSED%' or cm.`action` like '%REJECTED%') ORDER BY id desc limit 1",
            countQuery = "select count(*) from complaint_movements as cm \n" +
                    "where cm.complaint_id=?1 and (cm.`action` like '%CLOSED%' or cm.`action` like '%REJECTED%' or cm.`action` = 'FORWARDED_TO_AO') ORDER BY id desc limit 1",
            nativeQuery = true)
    public GrievanceForwarding findRecentlyClosedOrRejectedOne(Long grievanceId);

    public GrievanceForwarding findByIsCurrentAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(Boolean isCurrent, Long officeId, Long OfficeUnitOrganogramId, Grievance grievance);

    public GrievanceForwarding findByActionAndToOfficeIdAndToOfficeUnitOrganogramIdAndGrievance(String action, Long officeId, Long OfficeUnitOrganogramId, Grievance grievance);

    GrievanceForwarding findByGrievanceAndIsCurrentAndIsCommitteeHead(Grievance grievance, boolean isCurrent, boolean isCommitteeHead);

    public Page<GrievanceForwarding> findAll(Specification specification, Pageable pageable);

    public List<GrievanceForwarding> findAll(Specification specification);

    GrievanceForwarding findTopByGrievanceAndActionLikeOrderByIdDesc(Grievance grievance, String action);

    List<GrievanceForwarding> findByIsCurrentAndGrievanceAndIsCommitteeMember(boolean isCurrent, Grievance grievance, boolean isCommitteeMember);

    @Query(value = "select cm.* from complaint_movements as cm where complaint_id = ?1 AND\n" +
            "(cm.to_office_id = ?2 OR cm.from_office_id = ?2) AND\n" +
            "(cm.to_office_unit_organogram_id in ?3 OR cm.from_office_unit_organogram_id in ?3)AND\n" +
            "(cm.action NOT LIKE ?4)\n" +
            " ORDER BY cm.id DESC",
            countQuery = "select count(*) from complaint_movements as cm where complaint_id = ?1 AND\n" +
                    "(cm.to_office_id = ?2 OR cm.from_office_id = ?2) AND\n" +
                    " (cm.to_office_unit_organogram_id in ?3 OR cm.from_office_unit_organogram_id in ?3) AND\n" +
                    "(cm.action NOT LIKE ?4)\n" +
                    "ORDER BY cm.id DESC",
            nativeQuery = true)
    List<GrievanceForwarding> findByGrievanceIdAndOfficeAndOfficeUnitOrganogramInAndAction(Long grievanceId, Long officeId, List<Long> officeUnitOrganogramId, String action);

    @Query(value = "select cm.* from complaint_movements as cm where complaint_id = ?1 AND\n" +
            "(cm.to_employee_record_id = cm.from_employee_record_id OR cm.assigned_role LIKE ?2)\n" +
            " ORDER BY cm.id DESC",
            countQuery = "select count(*) from complaint_movements as cm where complaint_id = ?1 AND\n" +
                    "(cm.to_employee_record_id = cm.from_employee_record_id OR cm.assigned_role LIKE ?2)\n" +
                    " ORDER BY cm.id DESC",
            nativeQuery = true)
    List<GrievanceForwarding> findByGrievanceIdAndAssignedRole(Long grievanceId, String roleName);

    @Query(value = "select cm.* from complaint_movements as cm where complaint_id = ?1 AND\n" +
            "(cm.to_employee_record_id = cm.from_employee_record_id OR cm.assigned_role LIKE ?2 OR cm.action LIKE ?3)\n" +
            " ORDER BY cm.id DESC",
            countQuery = "select count(*) from complaint_movements as cm where complaint_id = ?1 AND\n" +
                    "(cm.to_employee_record_id = cm.from_employee_record_id OR cm.assigned_role LIKE ?2)\n" +
                    " ORDER BY cm.id DESC",
            nativeQuery = true)
    List<GrievanceForwarding> findByGrievanceIdAndAssignedRoleAndAction(Long grievanceId, String roleName, String action);

    @Query(value = "select cm.* from complaint_movements as cm where complaint_id = ?1 \n" +
            " ORDER BY cm.id DESC",
            countQuery = "select count(*) from complaint_movements as cm where complaint_id = ?1 \n" +
                    " ORDER BY cm.id DESC",
            nativeQuery = true)
    List<GrievanceForwarding> findByGrievanceId(Long grievanceId);

    @Query(value = "select cm.* from complaint_movements as cm where complaint_id = ?1 AND\n" +
            "(cm.to_office_id = ?2 OR cm.from_office_id = ?2) AND\n" +
            "(cm.to_office_unit_organogram_id in ?3 OR cm.from_office_unit_organogram_id in ?3)AND\n" +
            "(cm.action NOT LIKE ?4) AND\n" +
            "(cm.created_at >= ?5 and cm.created_at <= ?6)\n" +
            " ORDER BY cm.id DESC",
            countQuery = "select count(*) from complaint_movements as cm where complaint_id = ?1 AND\n" +
                    "(cm.to_office_id = ?2 OR cm.from_office_id = ?2) AND\n" +
                    " (cm.to_office_unit_organogram_id in ?3 OR cm.from_office_unit_organogram_id in ?3) AND\n" +
                    "(cm.action NOT LIKE ?4)AND\n" +
                    "(cm.created_at >= ?5 and cm.created_at <= ?6)\n" +
                    "ORDER BY cm.id DESC",
            nativeQuery = true)
    List<GrievanceForwarding> findByGrievanceIdAndOfficeAndOfficeUnitOrganogramInAndActionAndCreatedAt(Long grievanceId, Long officeId, List<Long> officeUnitOrganogramId, String action, Date start, Date finish);

    List<GrievanceForwarding> findByGrievanceAndActionLikeOrderByIdDesc(Grievance grievance, String action);

    @Query(value = "select * from complaint_movements where complaint_id = ?1 and action like ?2 and current_status like ?3", nativeQuery = true)
    GrievanceForwarding findByGrievanceAndActionLikeAndCurrentStatusLike(Long grievanceId, String action, String currentStatus);

    @Query(value = "select * from complaint_movements where complaint_id = ?1 and action like ?2 and current_status not like ?3", nativeQuery = true)
    GrievanceForwarding findByGrievanceAndActionLikeAndCurrentStatusNotLike(Long grievanceId, String action, String currentStatus);

    List<GrievanceForwarding> findByGrievanceAndIsCurrent(Grievance grievance, Boolean isCurrent);

    @Query(value = "select *  from complaint_movements where complaint_id = ?1 group by  to_office_id, to_office_unit_organogram_id",
            countQuery = "select *  from complaint_movements where complaint_id = ?1 group by  to_office_id, to_office_unit_organogram_id",
            nativeQuery = true)
    List<GrievanceForwarding> getDistinctToEmployeeRecordIdByGrievance(Long grievanceId);

    @Query(value = "select *  from complaint_movements  where complaint_id = ?1 AND from_employee_record_id = to_employee_record_id  AND assigned_role = 'COMPLAINANT'\n" +
            "ORDER BY id DESC LIMIT 0,1",
            nativeQuery = true)
    GrievanceForwarding getLatestComplainantMovement(Long grievanceId);

    @Query(
            value = "SELECT * FROM complaint_movements WHERE \n" +
                    "( (complaint_movements.to_office_id = ?1 AND complaint_movements.to_office_unit_organogram_id = ?2) OR \n" +
                    "(complaint_movements.from_office_id = ?1 AND complaint_movements.from_office_unit_organogram_id = ?2) ) AND \n" +
                    "complaint_movements.complaint_id IN (SELECT id FROM complaints WHERE complaints.office_id = ?1) And \n" +
                    "complaint_movements.is_current=1",
            nativeQuery = true
    )
    /*
    @Query(value = "select * from complaint_movements where id in ( " +
            "    select ast.id from (select max(id) as id, complaint_id " +
            "    from complaint_movements where ((complaint_movements.to_office_id = ?1 AND complaint_movements.to_office_unit_organogram_id = ?2) OR " +
            "    (complaint_movements.from_office_id = ?1 AND complaint_movements.from_office_unit_organogram_id = ?2) " +
            "    ) and is_current=1 group by complaint_id) ast " +
            ") and complaint_id in (SELECT id FROM complaints WHERE complaints.office_id = ?1)", nativeQuery = true)

     */
    List<GrievanceForwarding> getAllMovementsOfPreviousGRO(Long groOfficeId, Long groOfficeUnitOrganogramId);

    List<GrievanceForwarding> findByGrievanceAndActionLikeAndFromOfficeIdOrderByIdDesc(Grievance complaint, String action, Long officeId);

    @Query(value = "select * from complaint_movements cm where cm.to_office_id = ?1 and cm.to_office_unit_organogram_id = ?2 " +
            "and cm.is_current = ?3 and cm.is_cc = ?4 and cm.is_seen = ?5\n" +
            "and cm.`action` not like ('%CLOSED%') and cm.`action` not like '%APPEAL%' and cm.`action` not like '%REJECTED%'\n" +
            "and cm.complaint_id in (select id from complaints c where c.office_id = ?1 \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%CLOSED%') \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%REJECTED%')\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%APPEAL%'))" +
            "group by cm.complaint_id", nativeQuery = true)
    List<GrievanceForwarding> getUnseenInboxOrCCCount(Long officeId, Long officeUnitOrganogramId, Boolean isCurrent, Boolean isCC, Boolean isSeen);

    @Query(value = "select count(*) from complaint_movements cm cross join complaints c on c.id = cm.complaint_id\n" +
            "\t where cm.to_office_id = ?1 and cm.to_office_unit_organogram_id = ?2\n" +
            "            and cm.is_current = ?3 and cm.is_seen = ?4 and cm.is_cc = 0\n" +
            "            and cm.`action` not like ('%CLOSED%')\n" +
            "and cm.complaint_id in (select id from complaints cc where cc.office_id = ?1 \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand cc.current_status not like ('%CLOSED%') \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand cc.current_status not like ('%REJECTED%'))\n" +
            "            and c.current_appeal_office_id = ?1 and c.current_appeal_office_unit_organogram_id = ?2" , nativeQuery = true)
    Long getUnseenAppealCount(Long officeId, Long officeUnitOrganogramId, Boolean isCurrent, Boolean isSeen);

    @Query(value = "select * from complaint_movements cm where cm.to_office_id = ?1 and cm.to_office_unit_organogram_id = ?2 " +
            "and cm.is_current = ?3 and cm.is_cc = ?4\n" +
            "and cm.`action` not like ('%CLOSED%') and cm.`action` not like '%APPEAL%' and cm.`action` not like '%REJECTED%'\n" +
            "and cm.complaint_id in (select id from complaints c where c.office_id = ?1 \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%CLOSED%') \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%REJECTED%')\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand c.current_status not like ('%APPEAL%'))" +
            "group by cm.complaint_id", nativeQuery = true)
    List<GrievanceForwarding> getTotalInboxOrCCCount(Long officeId, Long officeUnitOrganogramId, Boolean isCurrent, Boolean isCC);

    @Query(value = "select count(*) from complaint_movements cm cross join complaints c on c.id = cm.complaint_id\n" +
            "\t where cm.to_office_id = ?1 and cm.to_office_unit_organogram_id = ?2\n" +
            "            and cm.is_current = ?3 and cm.is_cc = 0\n" +
            "            and cm.`action` not like ('%CLOSED%')\n" +
            "and cm.complaint_id in (select id from complaints cc where cc.office_id = ?1 \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand cc.current_status not like ('%CLOSED%') \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tand cc.current_status not like ('%REJECTED%'))\n" +
            "            and c.current_appeal_office_id = ?1 and c.current_appeal_office_unit_organogram_id = ?2" , nativeQuery = true)
    Long getTotalAppealCount(Long officeId, Long officeUnitOrganogramId, Boolean isCurrent);


    @Query(value = "select grievancef0_.id, grievancef0_.created_at,\n" +
            " grievancef0_.created_by, grievancef0_.modified_by,\n" +
            " grievancef0_.status, grievancef0_.modified_at,\n" +
            " grievancef0_.action, grievancef0_.assigned_role,\n" +
            " grievancef0_.note, grievancef0_.current_status,\n" +
            " grievancef0_.deadline_date,\n" +
            " grievancef0_.from_employee_designation_bng,\n" +
            " grievancef0_.from_employee_name_bng,\n" +
            " grievancef0_.from_employee_name_eng,\n" +
            " grievancef0_.from_employee_record_id,\n" +
            " grievancef0_.from_employee_unit_name_bng,\n" +
            " grievancef0_.from_employee_username,\n" +
            " grievancef0_.from_office_id,\n" +
            " grievancef0_.from_office_name_bng,\n" +
            " grievancef0_.from_office_unit_id,\n" +
            " grievancef0_.from_office_unit_organogram_id,\n" +
            " grievancef0_.complaint_id,\n" +
            " grievancef0_.is_cc, grievancef0_.is_committee_head,\n" +
            " grievancef0_.is_committee_member,\n" +
            " grievancef0_.is_current, grievancef0_.is_seen,\n" +
            " grievancef0_.to_employee_designation_bng,\n" +
            " grievancef0_.to_employee_name_bng,\n" +
            " grievancef0_.to_employee_name_eng,\n" +
            " grievancef0_.to_employee_record_id,\n" +
            " grievancef0_.to_employee_unit_name_bng,\n" +
            " grievancef0_.to_office_id,\n" +
            " grievancef0_.to_office_name_bng,\n" +
            " grievancef0_.to_office_unit_id,\n" +
            " grievancef0_.to_office_unit_organogram_id from complaint_movements grievancef0_ " +
            " cross join complaints grievance1_ " +
            " where grievancef0_.complaint_id=grievance1_.id " +
            " and grievance1_.complainant_id<>:userId " +
            " and (grievance1_.current_appeal_office_unit_organogram_id<>:officeUnitOrganogramId " +
            " or grievance1_.current_appeal_office_unit_organogram_id is null) " +
            " and (grievancef0_.from_office_unit_organogram_id=:officeUnitOrganogramId " +
            " and grievancef0_.from_office_id=:officeId and grievancef0_.to_office_unit_organogram_id<>:officeUnitOrganogramId " +
            //" or grievancef0_.to_office_unit_organogram_id=:officeUnitOrganogramId and grievancef0_.to_office_id=:officeId and grievancef0_.from_office_unit_organogram_id<>:officeUnitOrganogramId" +
            ") " +
            " and (grievancef0_.action not in  ('REJECTED' , 'FORWARDED_TO_AO' , 'FORWARD_TO_ANOTHER_OFFICE')) " +
//            " and (select count(grievancef4_.complaint_id) " +
//            " from complaint_movements grievancef4_, complaint_movements grievancef100_ " +
//            "where grievancef4_.to_office_unit_organogram_id=:officeUnitOrganogramId " +
//            " and grievancef4_.to_office_id=:officeId and grievancef4_.is_current=1 " +
//            " and grievancef4_.complaint_id=grievancef100_.complaint_id)=0 " +
            " and (grievance1_.current_status in ('NEW','ACCEPTED','IN_REVIEW','INVESTIGATION'," +
            "'INV_NOTICE_FILE','INV_NOTICE_HEARING','INV_HEARING','INV_REPORT','APPEAL_STATEMENT_ANSWERED'," +
            "'APPEAL_STATEMENT_ASKED','PERMISSION_ASKED','PERMISSION_REPLIED','GIVE_GUIDANCE','STATEMENT_ASKED'," +
            "'REQUEST_TESTIMONY','TESTIMONY_GIVEN'," +
            //"'STATEMENT_ANSWERED'," +
            "'INVESTIGATION_APPEAL'," +
            "'INV_NOTICE_FILE_APPEAL','INV_NOTICE_HEARING_APPEAL','INV_HEARING_APPEAL'," +
            "'INV_REPORT_APPEAL','APPEAL','APPEAL_IN_REVIEW','APPEAL_CLOSED_ACCUSATION_INCORRECT'," +
            "'APPEAL_CLOSED_OTHERS','APPEAL_CLOSED_ANSWER_OK','APPEAL_CLOSED_ACCUSATION_PROVED'," +
            "'APPEAL_CLOSED_INSTRUCTION_EXECUTED','APPEAL_CLOSED_SERVICE_GIVEN','APPEAL_REQUEST_TESTIMONY'," +
            "'APPEAL_GIVE_GUIDANCE','APPEAL_RECOMMMEND_DETARTMENTAL_ACTION','GIVE_GUIDANCE_POST_INVESTIGATION'," +
            "'APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION')) " +
            " group by grievancef0_.complaint_id order by grievance1_.modified_at desc \n-- #pageable\n",
            countQuery = "select count(grievancef0_.id) from complaint_movements grievancef0_ " +
                    " cross join complaints grievance1_ " +
                    " where grievancef0_.complaint_id=grievance1_.id " +
                    " and grievance1_.complainant_id<>:userId " +
                    " and (grievance1_.current_appeal_office_unit_organogram_id<>:officeUnitOrganogramId " +
                    " or grievance1_.current_appeal_office_unit_organogram_id is null) " +
                    " and (grievancef0_.from_office_unit_organogram_id=:officeUnitOrganogramId " +
                    " and grievancef0_.from_office_id=:officeId and grievancef0_.to_office_unit_organogram_id<>:officeUnitOrganogramId " +
                    //" or grievancef0_.to_office_unit_organogram_id=:officeUnitOrganogramId and grievancef0_.to_office_id=:officeId and grievancef0_.from_office_unit_organogram_id<>:officeUnitOrganogramId" +
                    ") " +
                    " and (grievancef0_.action not in  ('REJECTED' , 'FORWARDED_TO_AO' , 'FORWARD_TO_ANOTHER_OFFICE')) " +
//            " and (select count(grievancef4_.complaint_id) " +
//            " from complaint_movements grievancef4_, complaint_movements grievancef100_ " +
//            "where grievancef4_.to_office_unit_organogram_id=:officeUnitOrganogramId " +
//            " and grievancef4_.to_office_id=:officeId and grievancef4_.is_current=1 " +
//            " and grievancef4_.complaint_id=grievancef100_.complaint_id)=0 " +
                    " and (grievance1_.current_status in ('NEW','ACCEPTED','IN_REVIEW','INVESTIGATION'," +
                    "'INV_NOTICE_FILE','INV_NOTICE_HEARING','INV_HEARING','INV_REPORT','APPEAL_STATEMENT_ANSWERED'," +
                    "'APPEAL_STATEMENT_ASKED','PERMISSION_ASKED','PERMISSION_REPLIED','GIVE_GUIDANCE','STATEMENT_ASKED'," +
                    "'REQUEST_TESTIMONY','TESTIMONY_GIVEN'," +
                    //"'STATEMENT_ANSWERED'," +
                    "'INVESTIGATION_APPEAL'," +
                    "'INV_NOTICE_FILE_APPEAL','INV_NOTICE_HEARING_APPEAL','INV_HEARING_APPEAL'," +
                    "'INV_REPORT_APPEAL','APPEAL','APPEAL_IN_REVIEW','APPEAL_CLOSED_ACCUSATION_INCORRECT'," +
                    "'APPEAL_CLOSED_OTHERS','APPEAL_CLOSED_ANSWER_OK','APPEAL_CLOSED_ACCUSATION_PROVED'," +
                    "'APPEAL_CLOSED_INSTRUCTION_EXECUTED','APPEAL_CLOSED_SERVICE_GIVEN','APPEAL_REQUEST_TESTIMONY'," +
                    "'APPEAL_GIVE_GUIDANCE','APPEAL_RECOMMMEND_DETARTMENTAL_ACTION','GIVE_GUIDANCE_POST_INVESTIGATION'," +
                    "'APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION')) group by grievancef0_.complaint_id", nativeQuery = true)
    Page<GrievanceForwarding> findOutboxGrievance(@Param(value = "officeUnitOrganogramId") Long officeUnitOrganogramId, @Param(value = "officeId") Long officeId, @Param(value = "userId") Long userId, Pageable pageable);
}