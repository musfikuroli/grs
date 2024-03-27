package com.grs.core.repo.grs;

import com.grs.core.domain.grs.LoginTrace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Created by HP on 2/7/2018.
 */
@Repository
public interface LoginTraceRepo extends JpaRepository <LoginTrace, Long> {
    public LoginTrace findById(Long loginTraceId);
}
