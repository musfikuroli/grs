/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grs.core.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.grs.api.sso.SSOPropertyReader;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Orangebd
 */
public class IDP_Auth_OAuth {
    public static final String OAUTH2_REVOKE_URI = "https://idp.mygov.bd/oauth2/revoke";
    //    public static final String OAUTH2_TOKEN_URI = "https://idp.mygov.bd/oauth2/token";
//    public static final String OAUTH2_TOKEN_URI = "https://beta-idp.stage.mygov.bd/oauth/token";
    public static  String OAUTH2_TOKEN_URI = "https://idp-v2.live.mygov.bd/oauth/token";
    //    public static final String OAUTH2_REVOKE_URI = "https://idp.mygov.bd/oauth2/revoke";
//    public static final String OAUTH2_TOKEN_URI = "https://idp.mygov.bd/oauth2/token";
//    public static final String OAUTH2_AUTH_URL = "https://idp.mygov.bd/oauth2/auth";
//    public static final String OAUTH2_LOGOUT_URL = "https://idp.mygov.bd/oauth2/logout";
    public static final String OAUTH2_RESPONSE_TYPE = "code";
    public static final String OAUTH2_SCOPE = "openid";
    //    public static final String OAUTH2_AUTH_URL = "https://idp.mygov.bd/oauth2/auth";
//    public static final String OAUTH2_AUTH_URL = "https://beta-idp.stage.mygov.bd/oauth/authorize";
    public static String OAUTH2_AUTH_URL = "https://idp-v2.live.mygov.bd/oauth/authorize";
    //    public static final String OAUTH2_LOGOUT_URL = "https://idp.mygov.bd/oauth2/logout";
//    public static final String OAUTH2_LOGOUT_URL = "https://beta-idp.stage.mygov.bd/api/logout";
    public static String OAUTH2_LOGOUT_URL = "https://idp-v2.live.mygov.bd/api/logout";
    public static String OAUTH2_STAGE = "sBzBZfojjguQPJspBtN78YyGAsO1hkWTj5OEiHPI";
    private final HashMap<String, String> request = new HashMap<>();
    private HashMap<String, String> config = new HashMap<>();
    private String token;
    private String key;

    public IDP_Auth_OAuth() {
        try {
            OAUTH2_AUTH_URL = SSOPropertyReader.getInstance().getMyGovApiBaseUri() + "/oauth/authorize";
            OAUTH2_TOKEN_URI =SSOPropertyReader.getInstance().getMyGovApiBaseUri() +  "/oauth/token";
            OAUTH2_STAGE = SSOPropertyReader.getInstance().getMyGovApiState();
            OAUTH2_LOGOUT_URL =SSOPropertyReader.getInstance().getMyGovApiBaseUri() + "/api/logout";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String http_build_query(HashMap<String, String> array) {
        String reString = "";

        Iterator it = array.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<String, String> entry = (HashMap.Entry) it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (reString != "")
                reString += "&";
            reString += key + "=" + value;
        }
        return reString;
    }

    public String createAuthUrl(HashMap<String, String> config) {
        this.setConfig(config);
        this.generateToken();
        return this.buildLoginRequest();
    }

    public String createAuthUrl2(HashMap<String, String> config) {
        this.setConfig(config);
        return this.buildLoginRequest2();
    }

    public String createLogoutUrl(HashMap<String, String> config) {
        this.setConfig(config);
        this.generateToken();
        return this.buildLogoutRequest();
    }

    private void setConfig(HashMap<String, String> config) {
        this.config = config;
    }

    private String buildLoginRequest() {
        this.setRequest();
        return OAUTH2_AUTH_URL + "?" + http_build_query(this.request);
    }

    private String buildLoginRequest2() {
        this.setRequest2();
        return OAUTH2_AUTH_URL + "?" + http_build_query(this.request);
    }

    private String buildLogoutRequest() {
        this.setRequest();
        return OAUTH2_LOGOUT_URL + "?" + http_build_query(this.request);
    }

    private void setRequest2() {
        this.request.put("client_id", this.config.get("clientId"));
        this.request.put("redirect_uri", java.net.URLEncoder.encode(this.config.get("redirectUri")));
        this.request.put("response_type", this.OAUTH2_RESPONSE_TYPE);
        this.request.put("scope", this.OAUTH2_SCOPE);
        this.request.put("state", this.OAUTH2_STAGE);
    }

    private void setRequest() {
        this.request.put("client_id", this.config.get("clientId"));
        this.request.put("redirect_uri", java.net.URLEncoder.encode(this.config.get("redirectUri")));
        this.request.put("token", this.token);
    }

    private void generateToken() {
        InetAddress ip;
        String hostname = this.config.get("appHost");
        String ipAddress = "";

        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
            String tmp[] = String.valueOf(ip).split("/");
            ipAddress = tmp[tmp.length - 1];
        } catch (Exception ex) {
        }

        if (this.config.get("appHost") != "")
            this.config.put("appHost", hostname);
        this.config.put("ipAddress", ipAddress);
        this.config.put("time", String.valueOf(System.currentTimeMillis()).substring(0, 10));

        try {
            this.token = this.encrypt(this.config, this.config.get("clientSecret"));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public String responseRequest(HashMap<String, String> config, HttpServletRequest request) {
        try {
            String key = request.getParameter("key");
            String token = request.getParameter("token");
            String data = this.decrypt(token, key + this.MD5(config.get("clientSecret")));
            return data;
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    private String MD5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    private String encrypt(Map data, String key) throws Exception {
        try {
            Algorithm algorithm = Algorithm.HMAC256(key);
            return JWT.create()
                    //.withHeader(data)
                    .withClaim("clientId", String.valueOf(data.get("clientId")))
                    .withClaim("appHost", String.valueOf(data.get("appHost")))
                    .withClaim("ipAddress", String.valueOf(data.get("ipAddress")))
                    .withClaim("time", String.valueOf(data.get("time")))
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
        }
        return "";
    }

    private String decrypt(String token, String key) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(key);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            String[] split_string = token.split("\\.");
            String base64EncodedHeader = split_string[0];
            String base64EncodedBody = split_string[1];
            String base64EncodedSignature = split_string[2];

            System.out.println("~~~~~~~~~ JWT Header from MyGov parsed successfully ~~~~~~~");
//            String header = new String(Base64.getDecoder().decode(base64EncodedHeader));
//            System.out.println("JWT Header : " + header);
//
//
//            System.out.println("~~~~~~~~~ JWT Body ~~~~~~~");
            String body = new String(Base64.getDecoder().decode(base64EncodedBody));
//            System.out.println("JWT Body : "+body);

            return body;

        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return null;
        }
    }

    private String getToken(int length) {
        String token_local = "";
        String codeAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        codeAlphabet += "abcdefghijklmnopqrstuvwxyz";
        codeAlphabet += "0123456789";
        int max = codeAlphabet.length();

        for (int i = 0; i < length; i++) {
            token_local += codeAlphabet.charAt(this.crypto_rand_secure(0, max - 1));
        }
        return token_local;
    }

    private int crypto_rand_secure(int min, int max) {
        int range = max - min;
        if (range < 1) return min;
        int log_value = (int) Math.ceil(Math.log(range) / Math.log(2));
        int bytes = (int) (log_value / 8) + 1;
        int bits = (int) log_value + 1;
        int filter = (int) (1 << bits) - 1;
        int rnd = 0;
        do {
            rnd = (int) Math.floor(Math.random() * (bytes - min + 1)) + min;
            rnd = rnd & filter;
        } while (rnd > range);
        return min + rnd;
    }
}
