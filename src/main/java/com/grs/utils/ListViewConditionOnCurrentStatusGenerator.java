package com.grs.utils;

import com.grs.core.domain.GrievanceCurrentStatus;
import com.grs.core.model.ListViewType;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Acer on 02-Jan-18.
 */
public class ListViewConditionOnCurrentStatusGenerator {

    public static List<GrievanceCurrentStatus> getListOfCLosedOrRejectedStatus(){
        return Arrays.asList(
                GrievanceCurrentStatus.CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.CLOSED_OTHERS,
                GrievanceCurrentStatus.REJECTED
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListBasedOnListViewType(ListViewType listType) {
        switch (listType) {
            case NORMAL_INBOX:
                return getCurrentStatusListForNormalInbox();
            case NORMAL_CC:
                return getCurrentStatusListForNormalInbox();
            case NORMAL_OUTBOX:
                return getCurrentStatusListForNormalOutbox();
            case NORMAL_CLOSED:
                return getCurrentStatusListForNormalClosedList();
            case NORMAL_FORWARDED:
                return getCurrentStatusListForNormalForwardedList();
            case NORMAL_EXPIRED:
                return getCurrentStatusListForNormalExpiredList();
            case APPEAL_INBOX:
                return getCurrentStatusListForAppealInbox();
            case APPEAL_OUTBOX:
                return getCurrentStatusListForAppealOutbox();
            case APPEAL_CLOSED:
                return getCurrentStatusListForAppealClosedList();
            case APPEAL_EXPIRED:
                return getCurrentStatusListForAppealExpiredList();
        }
        return null;
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForNormalInbox() {
        return Arrays.asList(
                GrievanceCurrentStatus.NEW,
                GrievanceCurrentStatus.FORWARDED_OUT,
                GrievanceCurrentStatus.ACCEPTED,
                GrievanceCurrentStatus.IN_REVIEW,
                GrievanceCurrentStatus.INVESTIGATION,
                GrievanceCurrentStatus.FORWARDED_IN,
                GrievanceCurrentStatus.INV_NOTICE_FILE,
                GrievanceCurrentStatus.INV_NOTICE_HEARING,
                GrievanceCurrentStatus.INV_HEARING,
                GrievanceCurrentStatus.INV_REPORT,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.PERMISSION_ASKED,
                GrievanceCurrentStatus.PERMISSION_REPLIED,
                GrievanceCurrentStatus.GIVE_GUIDANCE,
                GrievanceCurrentStatus.STATEMENT_ASKED,
                GrievanceCurrentStatus.STATEMENT_ANSWERED,
                GrievanceCurrentStatus.RECOMMEND_DEPARTMENTAL_ACTION,
                GrievanceCurrentStatus.TESTIMONY_GIVEN,
                GrievanceCurrentStatus.REQUEST_TESTIMONY,
                GrievanceCurrentStatus.FORWARDED_TO_AO,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.CELL_NEW,
                GrievanceCurrentStatus.CELL_MEETING_ACCEPTED,
                GrievanceCurrentStatus.CELL_MEETING_PRESENTED,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForNormalOutbox() {
        return Arrays.asList(
                GrievanceCurrentStatus.NEW,
                GrievanceCurrentStatus.ACCEPTED,
                GrievanceCurrentStatus.IN_REVIEW,
                GrievanceCurrentStatus.INVESTIGATION,
                GrievanceCurrentStatus.INV_NOTICE_FILE,
                GrievanceCurrentStatus.INV_NOTICE_HEARING,
                GrievanceCurrentStatus.INV_HEARING,
                GrievanceCurrentStatus.INV_REPORT,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.PERMISSION_ASKED,
                GrievanceCurrentStatus.PERMISSION_REPLIED,
                GrievanceCurrentStatus.GIVE_GUIDANCE,
                GrievanceCurrentStatus.STATEMENT_ASKED,
                GrievanceCurrentStatus.REQUEST_TESTIMONY,
                GrievanceCurrentStatus.TESTIMONY_GIVEN,
                GrievanceCurrentStatus.STATEMENT_ANSWERED,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.APPEAL_CLOSED_OTHERS,
                GrievanceCurrentStatus.APPEAL_CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.APPEAL_CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.APPEAL_CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForNormalClosedList() {
        return Arrays.asList(
                GrievanceCurrentStatus.CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.CLOSED_OTHERS,
                GrievanceCurrentStatus.APPEAL_CLOSED_OTHERS,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.APPEAL_CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.APPEAL_CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.APPEAL_CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.REJECTED
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForNormalForwardedList() {
        return Arrays.asList(
                GrievanceCurrentStatus.NEW,
                GrievanceCurrentStatus.FORWARDED_OUT,
                GrievanceCurrentStatus.ACCEPTED,
                GrievanceCurrentStatus.IN_REVIEW,
                GrievanceCurrentStatus.INVESTIGATION,
                GrievanceCurrentStatus.FORWARDED_IN,
                GrievanceCurrentStatus.INV_NOTICE_FILE,
                GrievanceCurrentStatus.INV_NOTICE_HEARING,
                GrievanceCurrentStatus.INV_HEARING,
                GrievanceCurrentStatus.INV_REPORT,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.PERMISSION_ASKED,
                GrievanceCurrentStatus.PERMISSION_REPLIED,
                GrievanceCurrentStatus.GIVE_GUIDANCE,
                GrievanceCurrentStatus.STATEMENT_ASKED,
                GrievanceCurrentStatus.REQUEST_TESTIMONY,
                GrievanceCurrentStatus.TESTIMONY_GIVEN,
                GrievanceCurrentStatus.STATEMENT_ANSWERED,
                GrievanceCurrentStatus.FORWARDED_TO_AO,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_CLOSED_OTHERS,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.APPEAL_CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.APPEAL_CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.APPEAL_CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION/*,
                GrievanceCurrentStatus.REJECTED*/
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForNormalExpiredList() {
        return Arrays.asList(
                GrievanceCurrentStatus.NEW,
                GrievanceCurrentStatus.ACCEPTED,
                GrievanceCurrentStatus.IN_REVIEW,
                GrievanceCurrentStatus.INVESTIGATION,
                GrievanceCurrentStatus.INV_NOTICE_FILE,
                GrievanceCurrentStatus.INV_NOTICE_HEARING,
                GrievanceCurrentStatus.INV_HEARING,
                GrievanceCurrentStatus.INV_REPORT,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.PERMISSION_ASKED,
                GrievanceCurrentStatus.PERMISSION_REPLIED,
                GrievanceCurrentStatus.GIVE_GUIDANCE,
                GrievanceCurrentStatus.STATEMENT_ASKED,
                GrievanceCurrentStatus.TESTIMONY_GIVEN,
                GrievanceCurrentStatus.REQUEST_TESTIMONY,
                GrievanceCurrentStatus.STATEMENT_ANSWERED,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.FORWARDED_OUT,
                GrievanceCurrentStatus.FORWARDED_IN,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.RECOMMEND_DEPARTMENTAL_ACTION,
                GrievanceCurrentStatus.REQUEST_TESTIMONY,
                GrievanceCurrentStatus.FORWARDED_TO_AO,
                GrievanceCurrentStatus.CELL_NEW,
                GrievanceCurrentStatus.CELL_MEETING_ACCEPTED,
                GrievanceCurrentStatus.CELL_MEETING_PRESENTED,
                GrievanceCurrentStatus.GIVE_GUIDANCE_POST_INVESTIGATION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForAppealExpiredList() {
        return Arrays.asList(
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForAppealInbox() {
        return Arrays.asList(
                GrievanceCurrentStatus.APPEAL,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForAppealOutbox() {
        return Arrays.asList(
                GrievanceCurrentStatus.APPEAL_STATEMENT_ASKED,
                GrievanceCurrentStatus.APPEAL_STATEMENT_ANSWERED,
                GrievanceCurrentStatus.INVESTIGATION_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_FILE_APPEAL,
                GrievanceCurrentStatus.INV_NOTICE_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_HEARING_APPEAL,
                GrievanceCurrentStatus.INV_REPORT_APPEAL,
                GrievanceCurrentStatus.APPEAL_IN_REVIEW,
                GrievanceCurrentStatus.APPEAL_REQUEST_TESTIMONY,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE,
                GrievanceCurrentStatus.APPEAL_RECOMMMEND_DETARTMENTAL_ACTION,
                GrievanceCurrentStatus.APPEAL_GIVE_GUIDANCE_POST_INVESTIGATION
        );
    }

    public List<GrievanceCurrentStatus> getCurrentStatusListForAppealClosedList() {
        return Arrays.asList(
                GrievanceCurrentStatus.APPEAL_CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.APPEAL_CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.APPEAL_CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.APPEAL_CLOSED_OTHERS,
                GrievanceCurrentStatus.APPEAL_CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.CLOSED_SERVICE_GIVEN,
                GrievanceCurrentStatus.CLOSED_ANSWER_OK,
                GrievanceCurrentStatus.CLOSED_INSTRUCTION_EXECUTED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_PROVED,
                GrievanceCurrentStatus.CLOSED_ACCUSATION_INCORRECT,
                GrievanceCurrentStatus.CLOSED_OTHERS,
                GrievanceCurrentStatus.REJECTED
        );
    }

    public ListViewType getNormalListTypeByString(String listType) {
        switch (listType) {
            case "inbox":
                return ListViewType.NORMAL_INBOX;
            case "outbox":
                return ListViewType.NORMAL_OUTBOX;
            case "closed":
                return ListViewType.NORMAL_CLOSED;
            case "forwarded":
                return ListViewType.NORMAL_FORWARDED;
            case "expired":
                return ListViewType.NORMAL_EXPIRED;
            case "cc":
                return ListViewType.NORMAL_CC;
        }
        return null;
    }

    public ListViewType getAppealListTypeByString(String listType) {
        switch (listType) {
            case "inbox":
                return ListViewType.APPEAL_INBOX;
            case "outbox":
                return ListViewType.APPEAL_OUTBOX;
            case "closed":
                return ListViewType.APPEAL_CLOSED;
            case "expired":
                return ListViewType.APPEAL_EXPIRED;
        }
        return null;
    }
}