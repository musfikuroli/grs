package com.grs.core.repo.grs;

import com.grs.core.domain.grs.CountryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Acer on 02-Jan-18.
 */
@Repository
public interface NationalityAndCountryRepo extends JpaRepository<CountryInfo, Long>{

}
