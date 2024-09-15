package com.grs.core.service;

import com.grs.api.model.ComplainHistory;
import com.grs.core.repo.grs.BaseEntityManager;
import com.grs.utils.CalendarUtil;
import com.grs.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class GrievanceMigratorService {

    private final BaseEntityManager entityManager;

    public GrievanceMigratorService(BaseEntityManager entityManager) {
        this.entityManager = entityManager;
    }
    //@Scheduled(fixedDelay = Long.MAX_VALUE)
    //@Async("migrate1")
    public void migrate() {
        //---------Migration code--------

        String sql = "select min(id), max(id) from complaints ";
        Long min = 0L;
        Long max = 0L;
        try {

            Object[] result = entityManager.findSingleByQuery(sql, null);
            if (result != null && result.length >0) {
                if (Utility.valueExists(result, 0)) {
                    min = Utility.getLongValue(result[0]);
                }
                if (Utility.valueExists(result, 1)) {
                    max = Utility.getLongValue(result[1]);
                }
            }

            if (min == 0L && max == 0L) {
                log.info("====MIN is 0 and Max is 0 Migration stopped===");
                return;
            }

            for (Long i = min; i <= max; i++) {
                processGrievanceHistory(i);
            }

        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Migration error");
        }

    }

    public void processGrievanceHistory(Long i) {


        log.info("====Processing Grievance ID:{}", i);
        //----Pulling submitted--------
        String sql = "select com.id as complain_id, com.tracking_number as tracking_number, 'NEW' as current_status, com.office_id as office_id, ol.layer_level as layer_level," +
                "       ol.custom_layer_id as custom_layer, o.office_origin_id as office_origin,com.medium_of_submission as medium_of_submission, com.complaint_type as grievance_type," +
                " com.is_self_motivated_grievance as self_motivated, cm.created_at as created_at " +
                "from complaints com " +
                "    left join complaint_movements cm on com.id = cm.complaint_id " +
                "    left join grs_doptor.offices o on o.id = com.office_id " +
                "    left join grs_doptor.office_layers ol on o.office_layer_id = ol.id " +
                "where com.id =:complaintId and cm.id in (select max(id) from complaint_movements where complaint_id =:complaintId and action = 'NEW')";
        Map<String, Object> params = new HashMap<>();
        params.put("complaintId", i);
        com.grs.core.domain.grs.ComplainHistory newHistory = null;
        try {
            Object[] model = entityManager.findSingleByQuery(sql, params);
            if (model != null && model.length >0) {
                newHistory = new ComplainHistory(model).getComplainHistory();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (newHistory != null) {
            newHistory = entityManager.save(newHistory);
        }

        sql = "select com.id, com.tracking_number, 'CLOSED' as current_status, com.office_id, ol.layer_level,\n" +
                "       ol.custom_layer_id, o.office_origin_id,com.medium_of_submission, com.complaint_type, com.is_self_motivated_grievance, cm.modified_at\n" +
                "from complaints com\n" +
                "    left join complaint_movements cm on com.id = cm.complaint_id\n" +
                "    left join grs_doptor.offices o on o.id = com.office_id\n" +
                "    left join grs_doptor.office_layers ol on o.office_layer_id = ol.id\n" +
                "where com.id =:complaintId and cm.id in (select min(id) from complaint_movements\n" +
                "                                                where complaint_id =:complaintId \n" +
                "                                                  and action in ('REJECTED','CLOSED_SERVICE_GIVEN','CLOSED_ANSWER_OK',\n" +
                "                                                                 'CLOSED_INSTRUCTION_EXECUTED','CLOSED_ACCUSATION_PROVED',\n" +
                "                                                                 'CLOSED_ACCUSATION_INCORRECT','CLOSED_OTHERS'\n" +
                "                                                                )) ";

        com.grs.core.domain.grs.ComplainHistory closedHistory = null;
        try {
            Object[] model = entityManager.findSingleByQuery(sql, params);
            if (model != null && model.length >0) {
                closedHistory = new ComplainHistory(model).getComplainHistory();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // if (newHistory != null && closedHistory != null) {
        //     newHistory.setClosedAt(closedHistory.getCreatedAt());
        //     entityManager.merge(newHistory);
        // }
        if (newHistory != null && closedHistory != null) {
            // Get the createdAt from closedHistory and set time to 00:00:00
            newHistory.setClosedAt(CalendarUtil.truncateDate(closedHistory.getCreatedAt()));
            entityManager.merge(newHistory);
        }

        if (closedHistory != null) {
            entityManager.save(closedHistory);
        }

        sql = "select com.id, com.tracking_number, 'APPEAL' as current_status, com.office_id,ol.layer_level,\n" +
                "       ol.custom_layer_id, o.office_origin_id,com.medium_of_submission, com.complaint_type, com.is_self_motivated_grievance, cm.created_at \n" +
                "from complaints com\n" +
                "    left join complaint_movements cm on com.id = cm.complaint_id\n" +
                "    left join grs_doptor.offices o on o.id = com.office_id\n" +
                "    left join grs_doptor.office_layers ol on o.office_layer_id = ol.id\n" +
                "where com.id =:complaintId and cm.id in (select max(id) from complaint_movements\n" +
                "                                                where complaint_id =:complaintId \n" +
                "                                                  and action in ('APPEAL'\n" +
                "                                                                )) ";

        com.grs.core.domain.grs.ComplainHistory newAppeal = null;
        try {
            Object[] model = entityManager.findSingleByQuery(sql, params);
            if (model != null && model.length >0) {
                newAppeal = new ComplainHistory(model).getComplainHistory();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (newAppeal != null) {
            newAppeal = entityManager.save(newAppeal);
        }

        sql = "select com.id, com.tracking_number, 'APPEAL_CLOSED' as current_status, com.office_id,ol.layer_level,\n" +
                "       ol.custom_layer_id, o.office_origin_id,com.medium_of_submission, com.complaint_type, com.is_self_motivated_grievance, cm.modified_at\n" +
                "from complaints com\n" +
                "    left join complaint_movements cm on com.id = cm.complaint_id\n" +
                "    left join grs_doptor.offices o on o.id = com.office_id\n" +
                "    left join grs_doptor.office_layers ol on o.office_layer_id = ol.id\n" +
                "where com.id =:complaintId and cm.id in (select min(id) from complaint_movements\n" +
                "                                                where complaint_id =:complaintId \n" +
                "                                                  and action in ('APPEAL_CLOSED_SERVICE_GIVEN', 'APPEAL_CLOSED_ANSWER_OK',\n" +
                "                                                                 'APPEAL_CLOSED_INSTRUCTION_EXECUTED','APPEAL_CLOSED_ACCUSATION_PROVED',\n" +
                "                                                                'APPEAL_CLOSED_ACCUSATION_INCORRECT','APPEAL_REJECTED','APPEAL_CLOSED_OTHERS'\n" +
                "                                                                )) ";


        com.grs.core.domain.grs.ComplainHistory appealClosed = null;
        try {
            Object[] model = entityManager.findSingleByQuery(sql, params);
            if (model != null && model.length >0) {
                appealClosed = new ComplainHistory(model).getComplainHistory();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // if (newAppeal != null && appealClosed != null) {
        //     newAppeal.setClosedAt(appealClosed.getCreatedAt());
        // }

        if (newAppeal != null && appealClosed != null) {
            // Get the createdAt from appealClosed and set time to 00:00:00
            newAppeal.setClosedAt(CalendarUtil.truncateDate(appealClosed.getCreatedAt()));
        }

        sql = "select com.id, com.tracking_number, 'CELL_NEW' as current_status, com.office_id,ol.layer_level,\n" +
                "       ol.custom_layer_id, o.office_origin_id,com.medium_of_submission, com.complaint_type, com.is_self_motivated_grievance, cm.modified_at\n" +
                "from complaints com\n" +
                "    left join complaint_movements cm on com.id = cm.complaint_id\n" +
                "    left join grs_doptor.offices o on o.id = com.office_id\n" +
                "    left join grs_doptor.office_layers ol on o.office_layer_id = ol.id\n" +
                "where com.id =:complaintId and cm.id in (select max(id) from complaint_movements\n" +
                "                                                where complaint_id =:complaintId \n" +
                "                                                  and action ='CELL_NEW') ";

        com.grs.core.domain.grs.ComplainHistory cellNew = null;
        try {
            Object[] model = entityManager.findSingleByQuery(sql, params);
            if (model != null && model.length >0) {
                cellNew = new ComplainHistory(model).getComplainHistory();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (cellNew != null) {
            cellNew = entityManager.save(cellNew);
        }
        log.info("====Done for Grievance ID:{}", i);
    }
}
