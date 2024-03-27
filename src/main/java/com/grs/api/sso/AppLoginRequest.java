package com.grs.api.sso;

import com.grs.core.domain.RedirectMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.security.SecureRandom;

public class AppLoginRequest {
    SSOPropertyReader ssoPropertyReader;
    String landingPageUrl;
    String nonce;

    public AppLoginRequest()throws Exception{
        ssoPropertyReader = SSOPropertyReader.getInstance();
        this.landingPageUrl = "";
    }

    public void setLandingPageUrl(String landingPageUrl){
        this.landingPageUrl = landingPageUrl;
    }

    public String getNonce(){
        return this.nonce;
    }

    private String generateRandomHexToken(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16); //hex encoding
    }

    public String buildAuthnRequest(RedirectMap redirectMap) {
        if(this.landingPageUrl == null || this.landingPageUrl.equals("")){
            this.landingPageUrl = ssoPropertyReader.getLandingPageUri();
        }

        this.nonce = this.generateRandomHexToken(10);

        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(ssoPropertyReader.getIdpUrl() + "/" + "authorize" + "?");
        urlBuilder.queryParam("response_type","id_token");
        urlBuilder.queryParam("response_mode","form_post");
        urlBuilder.queryParam("client_id",ssoPropertyReader.getAppId());
        urlBuilder.queryParam("scope","openid");
        urlBuilder.queryParam("redirect_uri",ssoPropertyReader.getRedirectUri());
        urlBuilder.queryParam("landing_page_uri",this.landingPageUrl);
        urlBuilder.queryParam("state",this.generateRandomHexToken(10));
        urlBuilder.queryParam("nonce",this.nonce);
        urlBuilder.queryParam("redirect_flag", redirectMap);
        return urlBuilder.build().encode().toUriString();
    }
}