package com.grs.core.service;

import com.grs.utils.Constant;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Acer on 03-Jan-18.
 */
@Slf4j
@Service
public class ESBConnectorService {

    private String authToken;
    private RestTemplate restTemplate;

    @Value("${oisf.core.services.api.url}")
    private String baseUrl;

    @Value("${oisf.core.services.api.port}")
    private String port;

    @Value("${oisf.auth.secret.key}")
    private String authSecretKey;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });
        authToken = "";
    }

    public String getBaseUrlWithPort() {
        return baseUrl + ":" + port;
    }

    public Object getObjectFromESB(String URL, Class objectClass) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + Constant.OISF_ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(getBaseUrlWithPort() + URL, HttpMethod.GET, entity, objectClass);
        }catch (HttpClientErrorException e){
            log.error(e.getMessage());
            if(e.getStatusCode().value() == 401) {
                Object token = getAuthenticationToken();
                String auth = token== null ? null : token.toString();
                int attemptCount = 0;
                while (!StringUtil.isValidString(auth) && attemptCount < 10) {
                    token = getAuthenticationToken();
                    auth = token== null ? null : token.toString();
                    attemptCount++;
                }
                if (attemptCount >= 10) {
                    log.error("Could not get authentication token. Attempt count = " + attemptCount);
                    return null;
                }
                Constant.OISF_ACCESS_TOKEN = auth;
                headers.clear();
                headers.add("Authorization", "Bearer " + Constant.OISF_ACCESS_TOKEN);
                HttpEntity<String> newEntity = new HttpEntity<String>("parameters", headers);
                attemptCount = 0;
                while (attemptCount < 10){
                    try {

                        responseEntity = restTemplate.exchange(getBaseUrlWithPort() + URL, HttpMethod.GET, newEntity, objectClass);
                    } catch (Exception exception) {
                        // Output expected SocketTimeoutExceptions.
                        log.error(exception.getMessage());
                        attemptCount++;
                        continue;
                    }
                    break;
                }
            }
        }
        log.info(responseEntity.getStatusCode().getReasonPhrase());
        return responseEntity.getBody();
    }

    public Object getObjectFromESBWithoutToken(String URL, Class objectClass) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(getBaseUrlWithPort() + URL, HttpMethod.GET, entity, objectClass);
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            if (e.getStatusCode().value() == 401) {
                Object token = getAuthenticationToken();
                String auth = token == null ? null : token.toString();
                int attemptCount = 0;
                while (!StringUtil.isValidString(auth) && attemptCount < 10) {
                    token = getAuthenticationToken();
                    auth = token == null ? null : token.toString();
                    attemptCount++;
                }
                if (attemptCount >= 10) {
                    log.error("Could not get authentication token. Attempt count = " + attemptCount);
                    return null;
                }
                Constant.OISF_ACCESS_TOKEN = auth;
                headers.clear();
                headers.add("Authorization", "Bearer " + Constant.OISF_ACCESS_TOKEN);
                HttpEntity<String> newEntity = new HttpEntity<String>("parameters", headers);
                attemptCount = 0;
                while (attemptCount < 10) {
                    try {

                        responseEntity = restTemplate.exchange(getBaseUrlWithPort() + URL, HttpMethod.GET, newEntity, objectClass);
                    } catch (Exception exception) {
                        // Output expected SocketTimeoutExceptions.
                        log.error(exception.getMessage());
                        attemptCount++;
                        continue;
                    }
                    break;
                }
            }
        }
        log.info(responseEntity.getStatusCode().getReasonPhrase());
        return responseEntity.getBody();
    }



    public Object getAuthenticationToken(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Secret " + authSecretKey);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "client_credentials");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        WeakHashMap<String, String> resultObject = null;
        try {
            resultObject = restTemplate.postForObject(getBaseUrlWithPort() + "/token/create", request, WeakHashMap.class);
        } catch (HttpClientErrorException e){
            log.error(e.getMessage());
            return null;
        }
        return resultObject.get("token");
    }

    public WeakHashMap<String, String> getLoginAuthorization(String authToken, String userName, String password, String nonce){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + authToken);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("uname", userName);
        map.add("passwd", password);
        map.add("nonce", nonce);
        log.info(userName + " " + password + " " + getBaseUrlWithPort());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        WeakHashMap<String, String> resultObject = restTemplate.postForObject(getBaseUrlWithPort() + "/idp/api/verify", request, WeakHashMap.class);
        return resultObject;
    }

}
