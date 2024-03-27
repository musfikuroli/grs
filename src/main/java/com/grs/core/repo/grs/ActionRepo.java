package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Created by HP on 2/7/2018.
 */
@Repository
public interface ActionRepo extends JpaRepository <Action, Long> {
    public Action findByActionBng(String action);
    public Action findById(Long actionId);
}
