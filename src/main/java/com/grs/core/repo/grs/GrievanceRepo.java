package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.GrievanceCurrentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 9/14/2017.
 */
@Repository
public interface GrievanceRepo extends JpaRepository<Grievance, Long> {
    @Query(value="SELECT coalesce(max(CONVERT(tracking_number ,UNSIGNED INTEGER)),0) FROM complaints c where tracking_number not like '01%' and tracking_number not like '1%'",
    nativeQuery = true)
    public long findMaxTrackingNumber();

//    @Query("SELECT g FROM Grievance g WHERE g.trackingNumber = :trx")
//    List<Grievance> findGrievancesByTrackingNumber(@Param("trx") String trx);
    @Query("SELECT g FROM Grievance g WHERE g.trackingNumber = :trx OR g.trackingNumber = CONCAT('0', :trx)")
    List<Grievance> findGrievancesByTrackingNumber(@Param("trx") String trx);


    public Page<Grievance> findAllByOrderByCreatedAtDesc(Pageable pageable);

    public Page<Grievance> findByOfficeIdAndGrievanceCurrentStatusNotOrderByCreatedAtAsc(Long officeId, GrievanceCurrentStatus grievanceCurrentStatus, Pageable pageable);

    public Page<Grievance> findByOfficeIdAndGrievanceCurrentStatusOrderByCreatedAtDesc(Long officeId, GrievanceCurrentStatus currentStatus, Pageable pageable);

    public Page<Grievance> findByOfficeIdAndGrievanceCurrentStatusStartingWithOrderByCreatedAtDesc(Long officeId, String prefix, Pageable pageable);

    public Page<Grievance> findByOfficeIdAndGrievanceCurrentStatusInOrderByCreatedAtDesc(Long officeId, List<GrievanceCurrentStatus> currentStatusList, Pageable pageable);

    public Page<Grievance> findByComplainantIdAndGrsUserOrderByUpdatedAtDesc(Long userId, Pageable pageable, Boolean grsUser);

    public Page<Grievance> findByCreatedByAndSourceOfGrievanceOrderByUpdatedAtDesc(Long userId, Pageable pageable, String sourceOfGrievance);

    public List<Grievance> findByCreatedByAndSourceOfGrievanceOrderByUpdatedAtDesc(Long userId, String sourceOfGrievance);

    @Query(value = "select c.*\n"+
            "from complaints as c , (select cm.complaint_id from (select max(id) as id \n" +
            "from complaint_movements as cm\n" +
            "group by cm.complaint_id) as cmLatest , \n"+
            "complaint_movements as cm\n"+
            "where cmLatest.id=cm.id and (cm.current_status = 'APPEAL' || cm.action = 'APPEAL_STATEMENT_ASKED' ) and  cm.from_office_id=?1 and cm.from_office_unit_organogram_id=?2\n"+
            ") as outboxAppeal\n"+
            "where c.id=outboxAppeal.complaint_id and c.current_status not in ('APPEAL_REJECTED','APPEAL_CLOSED')\n"+
            "ORDER BY ?#{#pageable} ",
            countQuery =  "select count(*)\n"+
                    "from complaints as c , (select cm.complaint_id from (select max(id) as id \n"+
                    "from complaint_movements as cm\n" +
                    "group by cm.complaint_id) as cmLatest , \n"+
                    "complaint_movements as cm\n"+
                    "where cmLatest.id=cm.id and (cm.current_status = 'APPEAL' || cm.action = 'APPEAL_STATEMENT_ASKED' ) and  cm.from_office_id=?1 and cm.from_office_unit_organogram_id=?2\n"+
                    ") as outboxAppeal\n"+
                    "where c.id=outboxAppeal.complaint_id and c.current_status not in ('APPEAL_REJECTED','APPEAL_CLOSED')\n"+
                    "ORDER BY ?#{#pageable} ",
            nativeQuery = true)
    public Page<Grievance> getOutboxAppealGrievances(Long officeId, Long officeUnitOrganogramId, Pageable pageable);


    public Long countByOfficeId(Long officeId);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) FROM complaints AS c WHERE (c.current_status LIKE '%REJECTED%' OR c.current_status LIKE 'CLOSED%') AND office_id=?1")
    public Long getCountOfResolvedGrievancesByOfficeId(Long officeId);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) FROM complaints AS c WHERE " +
                    "(c.current_status NOT LIKE '%REJECTED%' " +
                    "AND c.current_status NOT LIKE 'CLOSED%' " +
                    "AND c.created_at < CURRENT_DATE - INTERVAL '2' MONTH) " +
                    "AND office_id=?1")
    public Long getCountOfUnresolvedGrievancesByOfficeId(Long officeId);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) FROM complaints AS c WHERE " +
                    "(c.current_status NOT LIKE '%REJECTED%' " +
                    "AND c.current_status NOT LIKE 'CLOSED%' " +
                    "AND c.created_at >= CURRENT_DATE - INTERVAL '2' MONTH) " +
                    "AND office_id=?1")
    public Long getCountOfRunningGrievancesByOfficeId(Long officeId);

    public Long countAllByOfficeId(Long officeId);

    public Page<Grievance> findByTrackingNumber(String trackingNumber, Pageable pageable);

    public Grievance findByTrackingNumber(String trackingNumber);

    public Grievance findByTrackingNumberAndComplainantId(String trackingNumber, Long complainantId);

    List<Grievance> findByIdIn(List<Long> grievanceIds);

    @Query(nativeQuery = true,
            value = "SELECT COUNT(*) " +
                    "FROM complaints AS c " +
                    "WHERE c.office_id=?1 AND c.service_id=?2")
    Long countByOfficeIdAndServiceOriginId(Long officeId, Long serviceOriginId);

    @Query(nativeQuery = true, value = "SELECT * FROM complaints AS c \n" +
            "WHERE c.office_id = ?1 AND c.current_status NOT LIKE '%CLOSED%' AND c.current_status NOT LIKE '%APPEAL%'")
    List<Grievance> findByOfficeIdAndStatus(Long officeId);

    @Query(nativeQuery = true, value = "select * from complaints where (complaints.current_appeal_office_id = 0 or office_id = 0) and current_status not like '%CLOSED%' \n" +
            "\tand current_status not like '%CELL_MEETING%' and current_status not like '%REJECTED%';")
    List<Grievance> findByCellOffice();

    public Page<Grievance> findAll(Specification specification, Pageable pageable);

}
