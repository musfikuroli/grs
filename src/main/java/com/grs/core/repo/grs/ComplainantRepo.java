package com.grs.core.repo.grs;

import com.grs.core.domain.grs.Complainant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Created by Acer on 9/27/2017.
 */
@Repository
public interface ComplainantRepo extends JpaRepository<Complainant, Long> {
    Complainant findByUsername(String Username);
    Complainant findByUsernameAndPassword(String Username, String password);
    Complainant findByPhoneNumber(String phoneNumber);
    List<Complainant> findByPhoneNumberIsContaining(String phoneNumber);
}
