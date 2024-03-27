package com.grs.core.repo.grs;

import com.grs.core.domain.grs.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by HP on 1/24/2018.
 */
@Repository
public interface SuperAdminRepo extends JpaRepository<SuperAdmin, Long>  {
    public SuperAdmin findByUsername(String Username);
    public SuperAdmin findByUsernameAndPassword(String Username, String password);

    @Query(value = "select c.*\n"+
            " from grs_users as c where c.user_role_id = ?1",
            nativeQuery = true)
    public List<SuperAdmin> findByUserRoleId(long role);

    public SuperAdmin findByPhoneNumber(String phoneNumber);
    public Integer countByUsername(String Username);
}
