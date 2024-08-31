package com.grs.api.model.response.dashboard.latest;

import com.grs.core.domain.grs.MonthlyReport;

public class GRSStatisticDTO {
    public long officeId;   //Id of an office
    public int year;        //Year of data
    public int month;       //Month of data
    public long totalSubmittedGrievance;      //count of total grievance submitted in this month
    public long currentMonthAcceptance; // count of grievance submitted in current month
    public long ascertainOfLastMonth;  //Count of ascertained grievances from last month
    public long runningGrievances;    //Count of grievances that are not yet resolved or rejected or forwarded to other office
    public long forwardedGrievances;  // Count of grievances that are forwarded to appeal officer or child offices GRO or GRO of any other office
    public long timeExpiredGrievances;// Count of grievances whose minimum days(60 days) allowed for resolving has been already expired
    public long resolvedGrievances;   // Count of grievances that are resolved within this month
    public float resolveRate;// Rate calculation of grievances that get resolved by excluding forwarded grievances from total grievances submitted
    public float rateOfAppealedGrievance;// Rate calculation of total submitted grievances of this office that are requested for appeal after getting resolved
    public long totalRating; // Count of grievances that get a rating by complainant after resolving
    public float averageRating;// Average of all accepted ratings during grievance resolve

    public long appealTotal;    // Count of total appeals get from immediate child offices
    public long appealCurrentMonthAcceptance;// count of appeal received in current month
    public long appealAscertain;        //Count of ascertained appeals from last month
    public long appealRunning;          //Count of appeals that are not resolved yet
    public long appealTimeExpired;      // Count of appeals whose minimum days(30 days) allowed for resolving has been already expired
    public long appealResolved;         //Count of appeals that are resolved within this month
    public float appealResolveRate;      // Rate calculation of total appeals that are get resolved by total appeals received

    public long subOfficesTotalGrievance;   //Aggregated sum of total grievances of all sub offices in this month up to leaf level
    public long subOfficesTimeExpiredGrievance;//Aggregated sum of time expired grievances of all sub offices in this month up to leaf level
    public long subOfficesResolvedGrievance;    //Aggregated sum of resolved grievances counts of all sub offices in this month up to leaf level
    public long subOfficesTotalAppeal;          //Aggregated sum of total appeal counts of all sub offices in this month up to leaf level
    public long subOfficesTimeExpiredAppeal;    //Aggregated sum of time expired appeal counts of all sub offices in this month up to leaf level
    public long subOfficesResolvedAppeal;       //Aggregated sum of resolved appeal counts of all sub offices in this month up to leaf level
    public float subOfficesGrievanceResolveRate; //Average of resolved grievances count by count of total grievances except forwarded to other offices of all sub offices in this month up to leaf level
    public float subOfficesAppealResolveRate;    //Average of resolved appeals count by count of total appeals of all sub offices in this month up to leaf level

    public GRSStatisticDTO() {

    }

    public GRSStatisticDTO(MonthlyReport monthlyReport) {
        if (monthlyReport != null) {
            this.officeId = monthlyReport.getOfficeId();
            this.month = monthlyReport.getMonth();
            this.year = monthlyReport.getYear();
            this.currentMonthAcceptance = 0;
            if (monthlyReport.getOnlineSubmissionCount() != null) {
                this.currentMonthAcceptance += monthlyReport.getOnlineSubmissionCount();
            }
            if (monthlyReport.getConventionalMethodSubmissionCount() != null) {
                this.currentMonthAcceptance += monthlyReport.getConventionalMethodSubmissionCount();
            }
            if (monthlyReport.getSelfMotivatedAccusationCount() != null) {
                this.currentMonthAcceptance += monthlyReport.getSelfMotivatedAccusationCount();
            }
            this.ascertainOfLastMonth = monthlyReport.getInheritedFromLastMonthCount();
            this.runningGrievances = monthlyReport.getRunningCount();
            this.forwardedGrievances = monthlyReport.getSentToOtherCount();
            this.timeExpiredGrievances = monthlyReport.getTimeExpiredCount();
            this.resolvedGrievances = monthlyReport.getResolvedCount();
            this.totalSubmittedGrievance = this.currentMonthAcceptance + this.ascertainOfLastMonth;
            Double rate = 0d;
            if (this.totalSubmittedGrievance >0) {
                Long totalDecided = monthlyReport.getResolvedCount() + monthlyReport.getSentToOtherCount();
                rate = ((double) totalDecided / (double) this.totalSubmittedGrievance) * 100;
                rate = (double) Math.round(rate * 100) / 100;
                if (rate >100) {
                    rate = 100d;
                }
            }

            this.resolveRate = rate.floatValue();
            this.appealCurrentMonthAcceptance =  monthlyReport.getAppealOnlineSubmissionCount();
            this.appealAscertain = monthlyReport.getAppealInheritedFromLastMonthCount();
            this.appealTotal = this.appealCurrentMonthAcceptance + this.appealAscertain;
            this.appealRunning = monthlyReport.getAppealRunningCount();
            this.appealTimeExpired = monthlyReport.getAppealTimeExpiredCount();
            this.appealResolved = monthlyReport.getAppealResolvedCount();

            Double appealRate = 0d;
            if (this.appealTotal > 0) {
                appealRate = ((double) monthlyReport.getAppealResolvedCount() / ((double) this.appealTotal)) * 100;
                appealRate = (double) Math.round(appealRate * 100) / 100;
            }
            this.appealResolveRate = appealRate.floatValue();
        }
    }
}
