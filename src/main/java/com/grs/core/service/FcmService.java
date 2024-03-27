package com.grs.core.service;

import com.grs.core.dao.FcmDAO;
import com.grs.core.domain.grs.FcmMessage;
import com.grs.core.domain.grs.FcmToken;
import com.grs.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FcmService {
    @Autowired
    private FcmDAO fcmDAO;
    @Autowired
    private EventService eventService;

    public static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    public static final String FCM_API_KEY = "AIzaSyDEA8f8X6WYDukQTMBXEUAL5gMcxmlBMpk";

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
    }

    public FcmToken registerDeviceToken(String deviceToken, String username) {
        FcmToken fcmToken = fcmDAO.getFcmTokenObjectByDeviceToken(deviceToken);
        Date now = new Date();
        if(fcmToken == null) {
            fcmToken = FcmToken.builder()
                    .deviceToken(deviceToken)
                    .username(username)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }
        if(!fcmToken.getUsername().equals(username)) {
            fcmToken.setUsername(username);
            fcmToken.setUpdatedAt(now);
        }
        return fcmDAO.saveFcmToken(fcmToken);
    }

    public boolean generatePushNotification(String to, FcmMessage fcmMessage) {
        return generatePushNotification(to, null, fcmMessage);
    }

    public boolean generatePushNotification(String[] registrationIds, FcmMessage fcmMessage) {
        return generatePushNotification(null, registrationIds, fcmMessage);
    }

    public boolean generatePushNotification(String to, String[] registrationIds, FcmMessage fcmMessage) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "key=" + FCM_API_KEY);
            httpHeaders.set("Content-Type", "application/json");

            JSONObject jsonObject = new JSONObject();
            if(StringUtil.isValidString(to)) {
                jsonObject.put("to", to);
            } else {
                jsonObject.put("registration_ids", registrationIds);
            }
            JSONObject data = new JSONObject();
            data.put("title", fcmMessage.getTitle());
            data.put("body", fcmMessage.getMessage());
            jsonObject.put("notification", data);

            HttpEntity<String> httpEntity = new HttpEntity(jsonObject.toString(), httpHeaders);
            HttpEntity<String> response = restTemplate.postForEntity(FCM_URL, httpEntity, String.class);
            log.info(response.toString());
            JSONObject responseBody = new JSONObject(response.getBody());
            return ((int)responseBody.get("success") > 0);
        } catch (JSONException ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    public Boolean sendPushNotification(String username, String message, String clickAction) {
        try {
            List<FcmToken> fcmTokenList = fcmDAO.getListOfFcmTokensByUsername(username);
            if(fcmTokenList.size() == 0) {
                return false;
            }
            List<String> deviceTokens = fcmTokenList.stream()
                    .map(t -> t.getDeviceToken())
                    .collect(Collectors.toList());
            String tokenIds = fcmTokenList.stream()
                    .map(t -> t.getId().toString())
                    .collect(Collectors.joining(", "));
            String[] registrationIds = deviceTokens.toArray(new String[deviceTokens.size()]);
            FcmMessage fcmMessage = FcmMessage.builder()
                    .username(username)
                    .message(message)
                    .title("GRS")
                    .clickAction(clickAction)
                    .tokenIds(tokenIds)
                    .type("INFO")
                    .createdAt(new Date())
                    .build();
            boolean isSent = generatePushNotification(registrationIds, fcmMessage);
            fcmMessage.setIsSent(isSent);
            if(isSent) {
                fcmMessage.setExpiredAt(new Date());
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.HOUR_OF_DAY, 6);
                fcmMessage.setExpiredAt(calendar.getTime());
            }
            fcmDAO.saveFcmMessage(fcmMessage);
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return false;
        }
    }

    @Scheduled(fixedDelay = 1800000, initialDelay = 1800000) //Default delay 30 minutes
    public void resendFailedMessages() {
        List<FcmMessage> fcmMessageList = fcmDAO.getUnsentUnexpiredMessages();
        for(FcmMessage fcmMessage: fcmMessageList) {
            boolean isSent;
            if(!fcmMessage.getTokenIds().contains(",")) {
                FcmToken fcmToken = fcmDAO.findFcmTokenById(Long.parseLong(fcmMessage.getTokenIds()));
                isSent = generatePushNotification(fcmToken.getDeviceToken(), fcmMessage);
            } else {
                List<Long> tokenIds = Arrays.asList(fcmMessage.getTokenIds().split(", "))
                        .stream()
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                List<FcmToken> fcmTokenList = fcmDAO.getListOfFcmTokensByIdInList(tokenIds);
                List<String> deviceTokens = fcmTokenList.stream()
                        .map(t -> t.getDeviceToken())
                        .collect(Collectors.toList());
                String[] registrationIds = deviceTokens.toArray(new String[deviceTokens.size()]);
                isSent = generatePushNotification(registrationIds, fcmMessage);
            }
            fcmMessage.setIsSent(isSent);
            fcmDAO.saveFcmMessage(fcmMessage);
        }
    }
}
