package com.grs.core.dao;

import com.grs.core.domain.grs.GrsRole;
import com.grs.core.repo.grs.GrsRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/28/2017.
 */
@Service
public class GrsRoleDAO {
    @Autowired
    private GrsRoleRepo grsRoleRepo;

    public GrsRole findOne(Long id) {
        return this.grsRoleRepo.findOne(id);
    }

    public GrsRole findByRole(String role){
        return this.grsRoleRepo.findByRole(role);
    }

    public List<GrsRole> findAll() {
        return this.grsRoleRepo.findAll();
    }
}
