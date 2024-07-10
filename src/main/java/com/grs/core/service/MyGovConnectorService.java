package com.grs.core.service;

import com.google.gson.Gson;
import com.grs.api.exception.DuplicateEmailException;
import com.grs.api.exception_handler.ApiErrorEnum;
import com.grs.api.model.request.ComplainantDTO;
import com.grs.api.myGov.*;
import com.grs.api.sso.SSOPropertyReader;
import com.grs.core.repo.grs.TempGrievanceRepository;
import com.grs.utils.Constant;
import com.grs.utils.DisableSSLCertificateCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by Acer on 03-Jan-18.
 */
@Slf4j
@Service
public class MyGovConnectorService {

    @Autowired
    ComplainantService complainantService;
    @Autowired
    Gson gson;

    @Autowired
    private TempGrievanceRepository repository;
    private RestTemplate restTemplate;

    private String baseUrl = "";
    private String myGovComplaintApiBaseUri = "";

    private String clientId = "";
    private String apiSecretKey = "";
    private long expirationTime = 0;
    private String cachedToken = "";

    @PostConstruct
    public void init() {
        restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            ClientHttpResponse response = execution.execute(request, body);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });
        try {

            expirationTime = new Date().getTime();
            clientId = SSOPropertyReader.getInstance().getMygovClientId();
            apiSecretKey = SSOPropertyReader.getInstance().getMygovApiSecret();
            baseUrl = SSOPropertyReader.getInstance().getMyGovApiBaseUri();
            myGovComplaintApiBaseUri = SSOPropertyReader.getInstance().getMyGovComplaintApiBaseUri();
            DisableSSLCertificateCheckUtil.disableChecks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getApiToken() throws Exception {

        if (new Date().getTime() < expirationTime && !cachedToken.isEmpty()) return cachedToken;

        HttpHeaders headers;
        headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.add("Authorization", "Secret " + secretKey);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("client_id", clientId);
        map.add("api_key", apiSecretKey);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        try {
            WeakHashMap<String, String> resultObject = restTemplate.postForObject(baseUrl + "/token/create", request, WeakHashMap.class);
            if (!resultObject.get("status").equalsIgnoreCase("success")) {
                throw new Exception(resultObject.get("message"));
            }
            cachedToken = (String) resultObject.get("token");
            expirationTime = new Date().getTime() + Constant.EXPIRATIONTIME;
            return resultObject.get("token");
        } catch (Exception e) {
            WeakHashMap<String, Object> returnObject = new WeakHashMap<String, Object>();
            returnObject.put("Exception in mygov api", baseUrl + "/token/create");
            returnObject.put("Request body", request);
            returnObject.put("Mygov Message", e.getMessage());
            throw new Exception(new Gson().toJson(returnObject));
        }
    }

    public boolean checkUser(String phoneNumber) throws Exception {
        String apiToken = getApiToken();

        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.add("Authorization", "Bearer " + authorizationToken);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("token", apiToken);
        map.add("mobile", phoneNumber);
        log.info(phoneNumber + " " + baseUrl);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        try {
            WeakHashMap<String, String> resultObject = restTemplate.postForObject(baseUrl + "/user/check", request, WeakHashMap.class);
            if (!resultObject.get("status").equalsIgnoreCase("success")) {
                throw new Exception(resultObject.get("message"));
            }
            return Boolean.parseBoolean(resultObject.get("user_exists"));
        } catch (Exception e) {
            WeakHashMap<String, Object> returnObject = new WeakHashMap<String, Object>();
            returnObject.put("Exception in mygov api", baseUrl + "/user/check");
            returnObject.put("Request body", request);
            returnObject.put("Mygov Message", e.getMessage());
            throw new Exception(new Gson().toJson(returnObject));
        }
    }

    public ComplainantDTO createUser(ComplainantDTO complainantDTO) throws Exception {
        String apiToken = getApiToken();
        MyGovLoginResponse myGovLoginResponse = new MyGovLoginResponse();
        MyGovUserDTO myGovUserDTO = myGovLoginResponse.genericDTOToClaims(complainantDTO);
        myGovUserDTO.setToken(apiToken);


        try {
            WeakHashMap<String, String> resultObject = restTemplate.postForObject(baseUrl + "/user/create", myGovUserDTO, WeakHashMap.class);
            if (!resultObject.get("status").equalsIgnoreCase("success")) {
                if (Integer.parseInt(resultObject.get("code")) == ApiErrorEnum.DUPLICATE_EMAIL_EXCEPTION.getValue())
                    throw new DuplicateEmailException(resultObject.get(String.valueOf(ApiErrorEnum.DUPLICATE_EMAIL_EXCEPTION.getValue())));
                throw new Exception(resultObject.get("message"));
            }
            String json = gson.toJson(resultObject.get("data"));
            myGovUserDTO = gson.fromJson(json, MyGovUserDTO.class);
            complainantDTO = myGovLoginResponse.claimToGenericDTO(myGovUserDTO);

            return complainantDTO;
        } catch (Exception e) {
            WeakHashMap<String, Object> returnObject = new WeakHashMap<String, Object>();
            returnObject.put("Exception in mygov api", baseUrl + "/user/create");
            returnObject.put("Request body", myGovUserDTO);
            returnObject.put("Code", e.getMessage());
            returnObject.put("Message", e.getMessage());
            if (e instanceof DuplicateEmailException) throw new DuplicateEmailException(e.getMessage());
            throw new Exception(new Gson().toJson(returnObject));
        }
    }

    /*
    public MyGovComplaintResponseDTO createComplaint(GrievanceWithoutLoginRequestDTO grievanceWithoutLoginRequestDTO) throws Exception {
        grievanceWithoutLoginRequestDTO.setComplainantPhoneNumber(BanglaConverter.convertAllToEnglish(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()));
        String complainantName = complainantService.findComplainantByPhoneNumber(grievanceWithoutLoginRequestDTO.getComplainantPhoneNumber()).getName();
        grievanceWithoutLoginRequestDTO.setName(complainantName);
        MyGovComplaintRequestDTO requestDTO = new MyGovComplaintRequestDTO();
        requestDTO = requestDTO.convertToMyGovComplaintRequestDTO(grievanceWithoutLoginRequestDTO);

        try {

            WeakHashMap<String, String> resultObject = restTemplate.postForObject(myGovComplaintApiBaseUri + "/api/getUaid", requestDTO, WeakHashMap.class);
            if (!resultObject.get("status").equalsIgnoreCase("success")) {
                throw new Exception(resultObject.get("message"));
            }
            return gson.fromJson(gson.toJson(resultObject), MyGovComplaintResponseDTO.class);
        } catch (Exception e) {
            log.error("9999 Mygov Error:", e);
            WeakHashMap<String, Object> returnObject = new WeakHashMap<>();
            returnObject.put("Exception in mygov api", myGovComplaintApiBaseUri + "/api/getUaid");
            returnObject.put("Request body", "আমার সরকার সিস্টেম এর সাথে যোগাযোগ সফল হয়নি। ");
            returnObject.put("Mygov Message", "আমার সরকার সিস্টেম এর সাথে যোগাযোগ করা সম্ভব হয়নি তাই আপনার অভিযোগ সঠিক ভাবে গ্রহণ করা হয়নি।  দয়া করে পরে আবারো চেষ্টা করুন।");
            throw new Exception(new Gson().toJson(returnObject));
        }
    }
*/
    public MyGovTokenResponse getMyGovToken(String code) {

        try {
            MyGovTokenRequest myGovTokenRequest = new MyGovTokenRequest(
                    "authorization_code",
                    SSOPropertyReader.getInstance().getMygovClientId(), SSOPropertyReader.getInstance().getMygovClientSecret(),
                    SSOPropertyReader.getInstance().getMyGovRedirectUrl(),
                    code);

            ResponseEntity<MyGovTokenResponse> responseResponseEntity = restTemplate.postForEntity(
                    SSOPropertyReader.getInstance().getMyGovApiBaseUri() + "/oauth/token",
                    myGovTokenRequest,
                    MyGovTokenResponse.class);

            if (responseResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                MyGovTokenResponse myGovTokenResponse = responseResponseEntity.getBody();
                log.info("MyGovTokenResponse : {} ", myGovTokenResponse);

                return myGovTokenResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public MyGovUser getMyGovLoginUser(String accessToken) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            ResponseEntity<HashMap> myGovUserResponseEntity = restTemplate.exchange(SSOPropertyReader.getInstance().getMyGovApiBaseUri() + "/api/user",
                    HttpMethod.GET,
                    new HttpEntity<String>(headers),
                    HashMap.class);


            if (myGovUserResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                MyGovUser myGovUser = gson.fromJson(gson.toJson(myGovUserResponseEntity.getBody().get("data")), MyGovUser.class);

                log.info("myGovUser : {} ", myGovUser);

                return myGovUser;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
