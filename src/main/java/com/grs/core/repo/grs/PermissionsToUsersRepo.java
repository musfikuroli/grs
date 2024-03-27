package com.grs.core.repo.grs;

import com.grs.core.domain.grs.PermissionsToUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Acer on 8/30/2017.
 */
@Repository
public interface PermissionsToUsersRepo extends JpaRepository <PermissionsToUsers, Long> {
    public List<PermissionsToUsers> findByUserIdAndUserType(Long userId, String userType);
}
