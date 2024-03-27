package com.grs.core.repo.projapoti;

import com.grs.core.domain.projapoti.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Tanvir on 8/13/2017.
 */
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    public User findByUsername(String Username);
    User findByEmployeeRecordId(Long id);
}
