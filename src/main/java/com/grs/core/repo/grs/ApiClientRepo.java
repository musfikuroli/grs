package com.grs.core.repo.grs;

import com.grs.core.domain.grs.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiClientRepo extends JpaRepository<ApiClient, Long> {
    ApiClient findByAppSecret(String appSecret);
    ApiClient findByAppSecretAndAppName(String appSecret,String appName);
    ApiClient findByAppName(String appName);
    ApiClient findByAccessToken(String accessToken);
    ApiClient findByRefreshToken(String refreshToken);
}
