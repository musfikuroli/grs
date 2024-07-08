package com.grs.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@Slf4j
public class ObjectFromAPIService {

    private String authToken;
    private RestTemplate restTemplate;

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

    public Object getObject(String url, Class objectClass, MultiValueMap<String, String> body) {
        init();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, objectClass);
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            log.error(entity.toString());
        }
        if (responseEntity == null) {
            return null;
        }
//        log.info(responseEntity.getStatusCode().getReasonPhrase());
        return responseEntity.getBody();
    }
}