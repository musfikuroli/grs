package com.grs.core.repo.grs;

import com.grs.core.domain.grs.FcmMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface FcmMessageRepo extends JpaRepository<FcmMessage, Long> {
    FcmMessage findByUsername(String username);
    List<FcmMessage> findByExpiredAtGreaterThanAndIsSentFalse(Date expiredAt);
}
