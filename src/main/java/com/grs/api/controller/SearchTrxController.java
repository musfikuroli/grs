package com.grs.api.controller;

import com.grs.core.domain.grs.Grievance;
import com.grs.core.repo.grs.GrievanceRepo;
import com.grs.core.service.ModelViewService;
import com.grs.utils.Utility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchTrxController {

    private final GrievanceRepo grievanceRepo;
    private final ModelViewService modelViewService;


    @GetMapping("/{trx_id}")
    public ResponseEntity<?> findByTrxId(@PathVariable("trx_id") String trx_id) {
        Long grievance_id = grievanceRepo.findGrievancesByTrackingNumber(trx_id).get(0).getId();

        if (grievance_id == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with tracking number " + trx_id + " not found.");
        }

        return ResponseEntity.ok(grievance_id);
    }


}
