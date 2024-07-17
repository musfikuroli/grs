package com.grs.core.repo.grs;

import com.grs.api.model.SafetyNetProgramDTO;
import com.grs.api.model.SafetyNetProgramReportResponse;
import com.grs.api.model.SafetyNetSummaryDTO;
import com.grs.api.model.SafetyNetSummaryResponse;
import com.grs.utils.CalendarUtil;
import com.grs.utils.Constant;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.reflections.util.Utils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Repository
public class BaseEntityManager {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(value = "transactionManager")
    public <T> T  findSingleByQuery(String nativeQuery, Class<T> tClass, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(nativeQuery, tClass);
        if (params != null && params.size() >0) {
            params.forEach(query::setParameter);
        }
        query.setFirstResult(0);
        query.setMaxResults(1);
        try {
            List list = query.getResultList();
            if (list != null && list.size() >0) {
                return (T) list.get(0);
            }
            return null;
        } catch (Throwable ne) {
            ne.printStackTrace();
            return null;
        }
    }

    @Transactional(value = "transactionManager")
    public <T> T  findSingleByQuery(String nativeQuery,Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(nativeQuery);
        if (params != null && params.size() >0) {
            params.forEach(query::setParameter);
        }
        try {
            return (T) query.getSingleResult();
        } catch (NoResultException ne) {
            return null;
        }
    }

    @Transactional(value = "transactionManager")
    public void updateYearlyStatistics() {
        Calendar cal = Calendar.getInstance();

        String sql = "delete from grs_current_year_statistics where id=" + cal.get(Calendar.YEAR);
        Query query = entityManager.createNativeQuery(sql);
        try {
            query.executeUpdate();
        } catch (Throwable t) {
            log.error("ERROR:", t);
            return;
        }

        query = entityManager.createNativeQuery("insert into grs_current_year_statistics(id,total_complaint, total_forwarded, total_resolved) " +
                "values (YEAR(NOW()), (SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data d " +
                "cross join complaint_movements cm " +
                "on cm.complaint_id = d.complaint_id " +
                "and cm.is_current = 1 " +
                "and cm.`action` not like '%APPEAL%' " +
                "and cm.current_status not like '%APPEAL%' " +
                "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                "AND (d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00')))," +
                " (SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data d cross join complaint_movements cm " +
                "on cm.complaint_id = d.complaint_id " +
                "and cm.is_current = 1 " +
                "and cm.`action` not like '%APPEAL%' " +
                "and cm.current_status not like '%APPEAL%' " +
                "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                "AND d.complaint_status LIKE 'FORWARDED%' " +
                "AND (d.created_at >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00')))," +
                " (SELECT COUNT(DISTINCT d.complaint_id) FROM dashboard_data AS d " +
                "cross join complaint_movements cm " +
                "on cm.complaint_id = d.complaint_id " +
                "and cm.is_current = 1 " +
                "and cm.`action` not like '%APPEAL%' " +
                "and cm.current_status not like '%APPEAL%' " +
                "WHERE d.complaint_status NOT LIKE '%APPEAL%' " +
                "AND (d.complaint_status LIKE 'CLOSED%' " +
                "OR d.complaint_status LIKE '%REJECTED%') " +
                "AND (d.closed_date >= DATE_FORMAT(CURDATE(), '%Y-01-01 00:00:00'))))");

        try {
            int result = query.executeUpdate();
            log.info("===CACHE UPDATED:{}", result);
        } catch (Throwable t) {
            log.error("ERROR:", t);
            return;
        }
    }

    @Transactional(value = "transactionManager")
    public <T> T save(T entity) {
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    @Transactional(value = "transactionManager")
    public <T> T merge(T entity) {
        entityManager.merge(entity);
        entityManager.flush();
        return entity;
    }


    @Transactional(value = "transactionManager")
    public void updateAllYearlyStatistics() {

//        String sql = "select id from grs_current_year_statistics where id>= year(created_at)";
//        Query query = entityManager.createNativeQuery(sql);
//        List<Long> years = new ArrayList<Long>();
//        try {
//            years = query.getResultList();
//        } catch (Throwable t) {
//            log.error("ERROR:", t);
//            return;
//        }
//
//        if(years.size()==0) return;
//        String ids=years.stream().map(e-> e.toString()).collect(Collectors.joining(","));

//        sql = "delete from grs_current_year_statistics where id in (" + ids+")";
        String sql = "delete from grs_current_year_statistics where 1=1";
        Query query = entityManager.createNativeQuery(sql);
        try {
            query.executeUpdate();
        } catch (Throwable t) {
            log.error("ERROR:", t);
            return;
        }

        query = entityManager.createNativeQuery(" " +
                "insert into grs_current_year_statistics(id, total_complaint, total_forwarded) " +
                " " +
                " " +
                "SELECT year(d.created_at), " +
                "       COUNT( " +
                "               DISTINCT " +
                "               case " +
                "                   when d.complaint_status NOT LIKE '%APPEAL%' " +
                "                       then d.complaint_id " +
                "                   end " +
                "           ) total_complaint, " +
                " " +
                "       COUNT( " +
                "               DISTINCT " +
                "               case " +
                "                   when d.complaint_status NOT LIKE '%APPEAL%' AND d.complaint_status LIKE 'FORWARDED%' " +
                "                       then d.complaint_id " +
                "                   end " +
                "           ) total_forwarded " +

                " " +
                " " +
                "FROM dashboard_data d " +
                "         cross join complaint_movements cm " +
                "                    on cm.complaint_id = d.complaint_id " +
                "                        and cm.is_current = 1 " +
                "                        and cm.`action` not like '%APPEAL%' " +
                "                        and cm.current_status not like '%APPEAL%' " +
//                " " + (year != null ? " where year(d.created_at)='" + year + "' " : "") +
                " " +
                " group by year(d.created_at) ");

        try {
            int result = query.executeUpdate();
            log.info("===CACHE UPDATED:{}", result);
        } catch (Throwable t) {
            log.error("ERROR:", t);
            return;
        }

        query = entityManager.createNativeQuery("update grs_current_year_statistics gcys inner join  " +
                "    (SELECT YEAR(d.closed_date) year,  " +
                "            COUNT(  " +
                "                    DISTINCT  " +
                "                    case  " +
                "                        when d.complaint_status NOT LIKE '%APPEAL%' AND  " +
                "                             (d.complaint_status LIKE 'CLOSED%' OR d.complaint_status LIKE '%REJECTED%')  " +
                "                            -- AND YEAR(d.created_at)=YEAR(d.closed_date)  " +
                "                            then d.complaint_id  " +
                "                        end  " +
                "                )               total_resolved  " +
                "  " +
                "  " +
                "     FROM dashboard_data d  " +
                "              cross join complaint_movements cm  " +
                "                         on cm.complaint_id = d.complaint_id  " +
                "                             and cm.is_current = 1  " +
                "                             and cm.`action` not like '%APPEAL%'  " +
                "                             and cm.current_status not like '%APPEAL%'  " +
//                "  " + (year != null ? " where year(d.closed_date)='" + year + "' " : "") +
                "  " +
                "     group by YEAR(d.closed_date)) x  " +
                "    on x.year = gcys.id  " +
                "set gcys.total_resolved=x.total_resolved,  " +
                " gcys.created_at=NOW()" +
                ";");

        try {
            int result = query.executeUpdate();
            log.info("===CACHE UPDATED:{}", result);
        } catch (Throwable t) {
            log.error("ERROR:", t);
            return;
        }


    }

    @Transactional(value = "transactionManager")
    public boolean deleteByQuery(String sql) {
        Query query = entityManager.createNativeQuery(sql);
        try {
            query.executeUpdate();
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    //TODO:: Date wise report
    public Long[] getDailyReport(Long layerLevel, Long officeOrigin, Long customLayer, Long officeId, Date fromDate, Date toDate) {

        String sql = "select count(distinct complain_id) from complain_history where current_status ='NEW' and created_at BETWEEN :fromDate and :toDate ";

        String where = " ";
        Map<String, Object> params = new HashMap<>();
        if (officeId != null && officeId != 9999) {
            where += " and office_id =:officeId ";
            params.put("officeId", officeId);
        }
//        if (layerLevel != null && !layerLevel.equals(9999L)) {
//            where += " and layer_level=:layerLevel ";
//            params.put("layerLevel", layerLevel);
//        }
//        if (officeOrigin != null && !officeOrigin.equals(9999L)) {
//            where += " and office_origin=:officeOrigin ";
//            params.put("officeOrigin", officeOrigin);
//        }
//        if (customLayer != null && !customLayer.equals(9999L)) {
//            where += " and custom_layer=:customLayer ";
//            params.put("customLayer", customLayer);
//        }

        Query query = entityManager.createNativeQuery(sql + where);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        Long totalSubmitted = 0L;
        Long resolvedCount = 0L;
        Long timeExpiredCount = 0L;
        Long runningGrievanceCount = 0L;
        Long sentToOtherOfficeCount = 0L;
        Long onlineSubmissionCount = 0L;
        Long conventionalMethodSubmissionCount = 0L;
        Long selfMotivatedAccusationCount = 0L;
        Long inheritedFromLastMonthCount = 0L;

        Long appealTotalSubmitted = 0L;
        Long appealResolved = 0L;
        Long appealExpired = 0L;
        Long appealRunning = 0L;
        Long appealOnlineSubmission = 0L;
        Long appealInheritedFromLastMonthCount = 0L;
        try {
            totalSubmitted = Long.parseLong(query.getSingleResult() + "");
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "select count(distinct complain_id) from complain_history where current_status ='CLOSED' " +
                "and created_at BETWEEN :fromDate and :toDate ";

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            resolvedCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }
//
        Long workDaysCountBefore = CalendarUtil.getWorkDaysCountBefore(fromDate, (int) Constant.GRIEVANCE_EXPIRATION_TIME);

        sql = "select count(distinct complain_id) from complain_history " +
                "where current_status in ('NEW', 'CLOSED') and created_at <:fromDate " +
                "and ((closed_at >:toDate or closed_at is null) and DATEDIFF(created_at, closed_at) > 0 and DATEDIFF(created_at, closed_at) >:workDaysCountBefore)";

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate)
                .setParameter("workDaysCountBefore", workDaysCountBefore);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            timeExpiredCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "select count(distinct complain_id) from complain_history where current_status = 'NEW' " +
                "and (closed_at is null or closed_at >:toDate) and created_at < :toDate ";

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            runningGrievanceCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "select count(distinct complain_id) from complain_history where current_status='FORWARDED_OUT' " +
                "and created_at BETWEEN :fromDate and :toDate ";
        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            sentToOtherOfficeCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "select count(distinct complain_id) from complain_history where current_status ='NEW' " +
                "and created_at BETWEEN :fromDate and :toDate and medium_of_submission=:mediumOfSubmission ";


        params.put("mediumOfSubmission", "ONLINE");

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            onlineSubmissionCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        params.put("mediumOfSubmission", "CONVENTIONAL_METHOD");

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            conventionalMethodSubmissionCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        params.put("mediumOfSubmission", "SELF_MOTIVATED_ACCEPTANCE");

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            selfMotivatedAccusationCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        params.remove("mediumOfSubmission");


        sql = "select count(distinct complain_id) from complain_history where current_status = 'NEW' " +
                "and (closed_at is null or closed_at > :fromDate) and created_at < :fromDate ";


        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            inheritedFromLastMonthCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        //// APPEAL

        sql = "SELECT count(DISTINCT  d.complaint_id ) FROM dashboard_data d " +
                "WHERE ((d.complaint_status LIKE '%APPEAL%' " +
                "AND d.complaint_status NOT LIKE 'APPEAL_CLOSED%' " +
                "AND d.complaint_status NOT LIKE 'APPEAL_REJECTED%' " +
                "AND (d.created_at BETWEEN :fromDate AND :toDate " +
                "OR d.created_at < :fromDate)) " +
                "or (d.complaint_status LIKE '%APPEAL%' " +
                "AND (d.created_at < :toDate AND (d.closed_date >= :fromDate or (d.is_forwarded=true and d.updated_at >= :fromDate ))))) ";

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealTotalSubmitted = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        sql = "SELECT COUNT(DISTINCT   d.complaint_id ) FROM dashboard_data AS d " +
                "WHERE (d.complaint_status LIKE 'APPEAL_CLOSED%' " +
                "OR d.complaint_status LIKE 'APPEAL_REJECTED%') " +
                "AND (d.closed_date BETWEEN :fromDate AND :toDate) ";

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealResolved = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        sql = "SELECT COUNT(DISTINCT  d.complaint_id) FROM dashboard_data AS d " +
                "WHERE d.complaint_status LIKE '%APPEAL%' " +
                "AND d.complaint_status NOT LIKE 'APPEAL_CLOSED%' " +
                "AND d.complaint_status NOT LIKE 'APPEAL_REJECTED%' " +
                "AND d.created_at < CURRENT_DATE - INTERVAL :date DAY";

        query = entityManager.createNativeQuery(sql + where).setParameter("date", Constant.APPEAL_EXPIRATION_TIME);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealExpired = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        sql = "SELECT count(distinct d.complaint_id)  " +
                "FROM dashboard_data d  " +
                "WHERE ((d.complaint_status LIKE '%APPEAL%' " +
                "AND d.complaint_status NOT LIKE 'APPEAL_CLOSED%'  " +
                "AND d.complaint_status NOT LIKE 'APPEAL_REJECTED%'  " +
                " AND (d.created_at BETWEEN   :fromDate  " +
                "  AND :toDate " +
                ")) " +
                "or " +
                "(d.complaint_status LIKE '%APPEAL%' " +
                " AND ( " +
                "(d.created_at BETWEEN :fromDate  " +
                "  AND :toDate   " +
                ") " +
                "AND " +
                "  (d.closed_date > :toDate  " +
                " or " +
                " (d.is_forwarded=true and d.updated_at > :toDate  ) " +
                " ) " +
                "))) "
        ;

        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealRunning = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "SELECT count(distinct d.complaint_id) " +
                "FROM dashboard_data d " +
                "WHERE d.complaint_status LIKE '%APPEAL%' " +
                "AND d.medium_of_submission='ONLINE' " +
                "AND (d.created_at BETWEEN :fromDate  " +
                "   AND :toDate  )  "
        ;
        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", fromDate)
                .setParameter("toDate", toDate);

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealOnlineSubmission = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }


        sql = "SELECT count(distinct d.complaint_id)   " +
                " FROM dashboard_data d   " +
                " WHERE d.complaint_status LIKE '%APPEAL%'   " +
                "  AND d.complaint_status NOT LIKE 'APPEAL_CLOSED%'   " +
                "  AND d.complaint_status NOT LIKE 'APPEAL_REJECTED%'   " +
                "  AND d.created_at <=:fromDate "

        ;
        query = entityManager.createNativeQuery(sql + where)
                .setParameter("fromDate", (fromDate));

        if (params.size() > 0) {
            params.forEach(query::setParameter);
        }
        try {
            appealInheritedFromLastMonthCount = Utility.getLongValue(query.getSingleResult());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        totalSubmitted += inheritedFromLastMonthCount;
        return new Long[]{totalSubmitted, resolvedCount, timeExpiredCount,
                runningGrievanceCount, sentToOtherOfficeCount,
                onlineSubmissionCount, conventionalMethodSubmissionCount,
                selfMotivatedAccusationCount, inheritedFromLastMonthCount,

                appealTotalSubmitted, appealResolved, appealExpired,
                appealRunning, appealOnlineSubmission, appealInheritedFromLastMonthCount

        };
    }

    private Date minusOneMonth(Date fromDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fromDate);
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    @Transactional(value = "transactionManager")
    public int updateByQuery(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        if (params != null && params.size() > 0) {
            params.forEach(query::setParameter);
        }

        try {
            return query.executeUpdate();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return 0;
    }

    @Transactional(value = "transactionManager")
    public String getTrackingNumber(String mobileNumber) {
        if (Utils.isEmpty(mobileNumber)) {
            return String.valueOf(System.currentTimeMillis());
        }
        if (!mobileNumber.startsWith("0")) {
            mobileNumber = "0" + mobileNumber;
        }
        String sql = "SELECT getNextSequence ('" + mobileNumber.trim() + "')";

        Query query = entityManager.createNativeQuery(sql);

        try {
            Object value = query.getSingleResult();
            return mobileNumber + Utility.leftPad(Utility.getLongValue(value), 4);//String.format("%04d", Integer.parseInt(String.valueOf(value) ));
        } catch (Throwable t) {
            t.printStackTrace();
            return mobileNumber + System.currentTimeMillis();
        }
    }

    @Transactional(value = "transactionManager")
    public SafetyNetProgramReportResponse safetyNetProgramReport(Long officeId, String fromDate, String toDate) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date fDate;
        Date tDate;
        try {
            fDate = df.parse(fromDate);
            tDate = df.parse(toDate);
        } catch (Throwable t) {
            t.printStackTrace();
            return new SafetyNetProgramReportResponse(false, "Date format should be:dd-MM-yyyy");
        }

        String sql = "select np.name_en, np.name_bn,\n" +
                "       SUM(IF(d.created_at between :fromDate and :toDate, 1,0)) as submitted,\n" +
                "       SUM(IF(d.created_at between :fromDate and :toDate\n" +
                "                  and (\n" +
                "                      ((d.complaint_status LIKE 'CLOSED_%' OR d.complaint_status LIKE '%REJECTED%') AND d.closed_date IS NOT NULL)\n" +
                "                          OR (d.complaint_status LIKE 'FORWARDED_%' and d.closed_date IS NOT NULL)\n" +
                "                          OR (d.complaint_status LIKE 'FORWARDED_OUT' AND d.closed_date IS NOT NULL)\n" +
                "                      ),1,0)) as resolved,\n" +
                "       SUM(IF(d.created_at < :fromDate and c.current_status NOT IN ('FORWARDED_OUT','REJECTED','CLOSED_ANSWER_OK','CLOSED_ACCUSATION_PROVED','CLOSED_ACCUSATION_INCORRECT','CLOSED_OTHERS'), 1,0)) as overdue\n" +
                "       from safety_net_program np,safety_net_grievance ng, dashboard_data d, complaints c\n" +
                "where np.id = ng.safety_net_id\n" +
                "  and ng.grievance_id = d.complaint_id\n" +
                "  and d.complaint_id = c.id\n" +
                "  and c.is_safety_net = true \n" ;

        if (officeId != null && officeId >0) {
            sql += " and d.office_id=:officeId and c.office_id=:officeId ";
        }

        sql += "group by np.name_en, np.name_bn ";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("fromDate", Utility.getDate(fDate, false));
        query.setParameter("toDate", Utility.getDate(tDate, true));
        if (officeId != null && officeId >0) {
            query.setParameter("officeId", officeId);
        }

        List<SafetyNetProgramDTO> programs = new ArrayList<>();
        try {
            List<Object[]> result = query.getResultList();
            if (result != null && result.size() >0) {
                for (Object[] objects : result) {
                    programs.add(new SafetyNetProgramDTO(objects, false));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return new SafetyNetProgramReportResponse(false, "Internal service error. Please contact with admin");
        }
        return new SafetyNetProgramReportResponse(programs);
    }

    public SafetyNetProgramReportResponse safetyNetProgramReportByProgramId(Long officeId, String fromDate, String toDate, Integer programId) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date fDate;
        Date tDate;
        try {
            fDate = df.parse(fromDate);
            tDate = df.parse(toDate);
        } catch (Throwable t) {
            t.printStackTrace();
            return new SafetyNetProgramReportResponse(false, "Date format should be:dd-MM-yyyy");
        }

        String sql = "select dis.district_name_eng, dis.district_name_bng,up.upazila_name_eng, up.upazila_name_bng,\n" +
                "       SUM(IF(d.created_at between :fromDate and :toDate, 1,0)) as submitted,\n" +
                "       SUM(IF(d.created_at between :fromDate and :toDate \n" +
                "                  and (\n" +
                "                      ((d.complaint_status LIKE 'CLOSED_%' OR d.complaint_status LIKE '%REJECTED%') AND d.closed_date IS NOT NULL)\n" +
                "                          OR (d.complaint_status LIKE 'FORWARDED_%' and d.closed_date IS NOT NULL)\n" +
                "                          OR (d.complaint_status LIKE 'FORWARDED_OUT' AND d.closed_date IS NOT NULL)\n" +
                "                      ),1,0)) as resolved,\n" +
                "       SUM(IF(d.created_at < :fromDate and c.current_status NOT IN ('FORWARDED_OUT','REJECTED','CLOSED_ANSWER_OK','CLOSED_ACCUSATION_PROVED','CLOSED_ACCUSATION_INCORRECT','CLOSED_OTHERS'), 1,0)) as overdue\n" +
                "       from safety_net_grievance ng left join safety_net_program np on np.id = ng.safety_net_id\n" +
                "       left join dashboard_data d on ng.grievance_id = d.complaint_id\n" +
                "       left join complaints c on d.complaint_id = c.id\n" +
                "       left join grs_doptor.geo_districts dis on dis.id = ng.district_id\n" +
                "       left join grs_doptor.geo_upazilas up on up.id = ng.upazila_id\n" +
                "where c.is_safety_net = true and ng.safety_net_id=:safetyNetId " ;

        if (officeId != null && officeId >0) {
            sql += " and d.office_id=:officeId and c.office_id=:officeId ";
        }

        sql += "group by dis.district_name_eng, dis.district_name_bng,up.upazila_name_eng, up.upazila_name_bng order by dis.district_name_eng asc";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("fromDate", Utility.getDate(fDate, false));
        query.setParameter("toDate", Utility.getDate(tDate, true));
        query.setParameter("safetyNetId", programId);
        if (officeId != null && officeId >0) {
            query.setParameter("officeId", officeId);
        }

        List<SafetyNetProgramDTO> programs = new ArrayList<>();
        try {
            List<Object[]> result = query.getResultList();
            if (result != null && result.size() >0) {
                for (Object[] objects : result) {
                    programs.add(new SafetyNetProgramDTO(objects, true));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return new SafetyNetProgramReportResponse(false, "Internal service error. Please contact with admin");
        }
        return new SafetyNetProgramReportResponse(programs);
    }

    public SafetyNetSummaryResponse getSafetyNetSummary(Long officeId, Integer programId) {

        String innerSQL = "";
        Map<String, Object> params = new HashMap<>();
        if (officeId != null && officeId >0) {
            innerSQL = " and c.office_id =:officeId ";
            params.put("officeId", officeId);
        }

        if (programId != null && programId >0) {
            innerSQL = " and sng.safety_net_id =:safetyNetId ";
            params.put("safetyNetId", programId);
        }

        String sql = "SELECT\n" +
                "    sub_type,\n" +
                "    SUM(IF(month = 'Jan', total, 0)) AS 'January',\n" +
                "    SUM(IF(month = 'Feb', total, 0)) AS 'February',\n" +
                "    SUM(IF(month = 'Mar', total, 0)) AS 'March',\n" +
                "    SUM(IF(month = 'Apr', total, 0)) AS 'April',\n" +
                "    SUM(IF(month = 'May', total, 0)) AS 'May',\n" +
                "    SUM(IF(month = 'Jun', total, 0)) AS 'June',\n" +
                "    SUM(IF(month = 'Jul', total, 0)) AS 'July',\n" +
                "    SUM(IF(month = 'Aug', total, 0)) AS 'August',\n" +
                "    SUM(IF(month = 'Sep', total, 0)) AS 'September',\n" +
                "    SUM(IF(month = 'Oct', total, 0)) AS 'October',\n" +
                "    SUM(IF(month = 'Nov', total, 0)) AS 'November',\n" +
                "    SUM(IF(month = 'Dec', total, 0)) AS 'December',\n" +
                "    SUM(total) AS total_yearly\n" +
                "FROM (\n" +
                "         SELECT sng.sub_type as sub_type,DATE_FORMAT(c.created_at, '%b') AS month, count(c.id) as total\n" +
                "         FROM complaints c, safety_net_grievance sng\n" +
                "         WHERE c.id = sng.grievance_id and c.created_at <= NOW() and c.created_at >= DATE_ADD(Now(),interval - 12 month)\n" +
                "         and c.is_safety_net = true "+innerSQL +
                "         GROUP BY sng.sub_type,DATE_FORMAT(created_at, '%m-%Y')) as sub\n" +
                "group by sub_type";

        List<SafetyNetSummaryDTO> summaryDTOS = new ArrayList<>();

        try {
            Query query = entityManager.createNativeQuery(sql);
            if (params.size() >0) {
                params.forEach(query::setParameter);
            }
            List<Object[]> results = query.getResultList();
            if (results != null && results.size() >0) {
                for (Object[] r : results) {
                    summaryDTOS.add(new SafetyNetSummaryDTO(r));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return new SafetyNetSummaryResponse(false, "Internal service error. Please contact with admin");
        }

        return new SafetyNetSummaryResponse(summaryDTOS);
    }

    @Transactional(value = "transactionManager")
    public Long  findMaxId(String nativeQuery,Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(nativeQuery);
        if (params != null && params.size() >0) {
            params.forEach(query::setParameter);
        }

        try {
            Object value = query.getSingleResult();
            return Utility.getLongValue(value);
        } catch (NoResultException ne) {
            return null;
        }
    }
}
