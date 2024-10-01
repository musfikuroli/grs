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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchTrxController {

    private final GrievanceRepo grievanceRepo;
    @GetMapping("/trx/{trx_id}")
    public ResponseEntity<?> findByTrxId(@PathVariable("trx_id") String trx_id) {
        List<Grievance> grievances = grievanceRepo.findGrievancesByTrackingNumber(trx_id);

        if (grievances.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Grievance with tracking number " + trx_id + " not found.");
        }

        if (grievances.size() == 1) {
            Long grievance_id = grievances.get(0).getId();
            Map<String, Object> response = new HashMap<>();
            response.put("single", true);
            response.put("id", grievance_id);
            return ResponseEntity.ok(response);
        }

        // If there are multiple grievances, return the list to be displayed
        Map<String, Object> response = new HashMap<>();
        response.put("single", false);
        response.put("grievances", grievances);
        return ResponseEntity.ok(response);
    }

}
