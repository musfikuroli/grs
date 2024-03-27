package com.grs.core.dao;

import com.grs.core.domain.grs.Action;
import com.grs.core.repo.grs.ActionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HP on 2/7/2018.
 */
@Service
public class ActionDAO {
    @Autowired
    private ActionRepo actionRepo;

    public Action findOne(Long id) {
        return actionRepo.findById(id);
    }

    public List<Action> findAll() {
        return this.actionRepo.findAll();
    }

    public Action getActionByActionName(String action) {
        return this.actionRepo.findByActionBng(action);
    }
}
