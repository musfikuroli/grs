package com.grs.core.dao;

import com.grs.api.model.UserType;
import com.grs.core.domain.grs.PermissionsToUsers;
import com.grs.core.repo.grs.PermissionsToUsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Acer on 9/28/2017.
 */
@Service
public class PermissionsToUsersDAO {
    @Autowired
    private PermissionsToUsersRepo permissionsToUsersRepo;

    public PermissionsToUsers findOne(Long id) {
        return this.permissionsToUsersRepo.findOne(id);
    }

    public List<PermissionsToUsers> findByUserIdAndUserType(Long userId, String userType){
        return this.permissionsToUsersRepo.findByUserIdAndUserType(userId, userType);
    }

    public List<PermissionsToUsers> findByOisfUserId(Long userId){
        return this.findByUserIdAndUserType(userId, UserType.OISF_USER.name());
    }

    public List<PermissionsToUsers> findAll() {
        return this.permissionsToUsersRepo.findAll();
    }
}
