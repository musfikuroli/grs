package com.grs.core.dao;

import com.grs.core.domain.grs.DashboardData;
import com.grs.core.repo.grs.ReportsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by Acer on 22-Feb-18.
 */
@Slf4j
@Service
public class ReportsDAO {
    @Autowired
    private ReportsRepo reportsRepo;

    public Long countByOfficeAndMediumOfSubmissionAndDateInBetween(Long officeId, String mediumOfSubmission, Date date) {
        return this.reportsRepo.countByOfficeAndMediumOfSubmissionAndDateInBetween(officeId, mediumOfSubmission, date);
    }

    public Long countResolvedGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date) {
        return this.reportsRepo.countResolvedGrievancesByOfficeIdAndDateInBetween(officeId, date);
    }

    public Long countUnresolvedGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date) {
        return this.reportsRepo.countUnresolvedGrievancesByOfficeIdAndDateInBetween(officeId, date);
    }

    public Long countRunningGrievancesByOfficeIdAndDateInBetween(Long officeId, Date date) {
        return this.reportsRepo.countRunningGrievancesByOfficeIdAndDateInBetween(officeId, date);
    }
}
