package com.grs.core.aop;

import com.grs.core.domain.grs.Grievance;
import com.grs.core.domain.grs.GrievanceForwarding;
import com.grs.core.service.DashboardService;
import com.grs.core.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Acer on 01-Feb-18.
 */
@Slf4j
@Aspect
@Component
public class GrievanceForwardingAspect {
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private EventService eventService;

    @Pointcut("execution(public * com.grs.core.dao.GrievanceForwardingDAO.forwardGrievance*(..))")
    public void forwardingGrievanceBackAndForthFromInboxMethods() {}

    @Pointcut("execution(public * com.grs.core.dao.GrievanceForwardingDAO.addNewHistory(..))")
    public void newGrievanceCreationMethods() {}

    @Pointcut("forwardingGrievanceBackAndForthFromInboxMethods() || newGrievanceCreationMethods()")
    public void grievanceForwardingCreationMethods() {}

    @Pointcut("execution(public * com.grs.core.dao.GrievanceDAO.feedbackAgainstGrievance(..))")
    public void getFeedbackAgainstGrievance() {}

    @Pointcut("execution(public * com.grs.core.dao.GrievanceDAO.feedbackAgainstAppealGrievance(..))")
    public void getFeedbackAgainstAppealGrievance() {}

    @After("grievanceForwardingCreationMethods()")
    public void afterAddNewHistoryMethodCall(JoinPoint jp) throws Throwable {
        /*
        log.info("joinpoint argument string: " + jp.getArgs().toString());
        log.info("joinpoint argument kind: " + jp.getKind());
        log.info("joinpoint argument longstring: " + jp.toLongString());
        log.info("joinpoint argument shortstring: " + jp.toShortString());
        log.info("joinpoint argument getSignature: "+ jp.getSignature().toString());
        log.info("joinpoint argument getSourceLocation: " + jp.getSourceLocation().toString());
        log.info("joinpoint argument getStaticPart: " +jp.getStaticPart().toLongString());
        log.info("joinpoint argument getTarget: " +jp.getTarget().toString());
        */
    }

    @AfterReturning(pointcut = "grievanceForwardingCreationMethods()", returning = "grievanceForwarding")
    public void executeOnGrievanceForwarding(GrievanceForwarding grievanceForwarding) throws Throwable {
        dashboardService.putDashboardDataRecord(grievanceForwarding);
        if(grievanceForwarding != null) {
            eventService.publish("send-push-notification-on-grievance-forwarding", grievanceForwarding);
        }
    }

    @AfterReturning(pointcut = "getFeedbackAgainstGrievance()", returning = "grievance")
    public void getFeedbackForDashboardData(Grievance grievance) {
        this.dashboardService.getFeedbackForDashboardData(grievance);
    }

    @AfterReturning(pointcut = "getFeedbackAgainstAppealGrievance()", returning = "grievance")
    public void getAppealFeedbackForDashboardData(Grievance grievance) {
        this.dashboardService.getAppealFeedbackForDashboardData(grievance);
    }
}
