package com.grs.core.repo.grs;

import com.grs.core.domain.*;
import com.grs.core.domain.grs.CitizenCharter;
import com.grs.core.domain.grs.ServiceOrigin;
import com.grs.core.domain.projapoti.Office;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Tanvir on 9/12/2017.
 */
@Repository
public interface CitizenCharterRepo extends JpaRepository<CitizenCharter, Long> {

    CitizenCharter findByOfficeIdAndServiceOriginId(Long officeId, Long serviceOriginId);

    List<CitizenCharter> findByOfficeId(Long officeId);

    CitizenCharter findByOfficeIdAndServiceOrigin(Long officeId, ServiceOrigin serviceOrigin);

    Page<CitizenCharter> findByOfficeIdOrderByIdAsc(Long officeId, Pageable pageable);

    @Query(
            nativeQuery = true,
            value = "select count(*) \n" +
                    "from offices_citizen_charter\n" +
                    "where service_id=:service_id and office_id=:office_id\n")
    Integer countByServiceIdAndOfficeId(@Param("service_id") Long serviceId, @Param("office_id") Long officeId);

    @Query(
            nativeQuery = true,
            value = "select * \n" +
                    "from offices_citizen_charter\n" +
                    "where service_id=:service_id and office_id=:office_id\n")
    CitizenCharter findByServiceIdAndOfficeId(@Param("service_id") Long serviceId, @Param("office_id") Long officeId);


    List<CitizenCharter> findByServiceOrigin(ServiceOrigin serviceOrigin);

    List<CitizenCharter> findByIdIn(List<Long> idList);

    List<CitizenCharter> findByOfficeIdAndServiceOriginServiceTypeAndStatusTrueAndOriginStatusTrue(Long officeId, ServiceType serviceType);
}
