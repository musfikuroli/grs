package com.grs.api.sso;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by USER on 13-Jan-18.
 */
public class InterAppLoginRequest {
    private String username = "";

    public InterAppLoginRequest(String username)throws Exception{
        if(username == null || username.equals("")){
            throw new Exception("Invalid user name");
        }

        this.username = username;
    }

    public String getToken(String toAppName, String landingPgUrl)throws Exception{

        String sharedSecret = SSOPropertyReader.getInstance().getSecret();
        String fromAppId = SSOPropertyReader.getInstance().getAppId();
        String fromAppName = SSOPropertyReader.getInstance().getAppName();
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        JwtBuilder jwts = null;

        jwts = Jwts.builder()
                .claim(JwtSSOClaims.FROM_APP_NAME,fromAppName)
                .claim(JwtSSOClaims.TO_APP_NAME,toAppName)
                .claim(JwtSSOClaims.LANDING_PAGE_URL,landingPgUrl)
                .claim(JwtSSOClaims.USERNAME,username)
                .claim("expirationTime",this.getExpiryTime())
                .signWith(signatureAlgorithm,sharedSecret.getBytes("UTF-8"));

        return jwts.compact();
    }

    public String getPostUrl()throws Exception{
        return SSOPropertyReader.getInstance().getIdpUrl() + "/" + "interapplogin";
    }

    public long getExpiryTime(){
        long curUtc = System.currentTimeMillis();
        return curUtc + Long.parseLong("180000");
    }
}
