package com.grs.core.dao;

import com.grs.core.domain.grs.ApiClient;
import com.grs.core.repo.grs.ApiClientRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApiClientDAO {
    @Autowired
    private ApiClientRepo apiClientRepo;

    public ApiClient get(Long id) {
        return apiClientRepo.findOne(id);
    }

    public ApiClient save(ApiClient apiClient) {
        return apiClientRepo.save(apiClient);
    }

    public ApiClient getByAppSecret(String appSecret) {
        return apiClientRepo.findByAppSecret(appSecret);
    }

    public ApiClient getByAppSecretAndAppName(String appSecret, String appName) {
        return apiClientRepo.findByAppSecretAndAppName(appSecret, appName);
    }

    public ApiClient getByAppName(String appName) {
        return apiClientRepo.findByAppName(appName);
    }

    public ApiClient getByAccessToken(String accessToken) {
        return apiClientRepo.findByAccessToken(accessToken);
    }

    public ApiClient getByRefreshToken(String refreshToken) {
        return apiClientRepo.findByRefreshToken(refreshToken);
    }
}
