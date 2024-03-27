/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.grs.core.config;

import com.grs.api.sso.SSOPropertyReader;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * @author Orangebd
 */
public class IDP_Client {
    private HashMap<String, String> config = new HashMap<String, String>();

    public IDP_Client() {
        this.config.put("appHost", "");
    }

    public IDP_Client(String redirectUri) {
        this.config.put("redirectUri", redirectUri);
        this.config.put("appHost", "");
    }

    private void init() {
        try {
            this.config.put("clientId", SSOPropertyReader.getInstance().getMygovClientId());
            this.config.put("clientSecret", SSOPropertyReader.getInstance().getMygovClientSecret());
            if (!this.config.containsKey("redirectUri") || this.config.get("redirectUri").isEmpty()) {
                this.config.put("redirectUri", SSOPropertyReader.getInstance().getBaseUri() + "/afterLoginFromMyGov");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setClientId(String clientId) {
        this.setConfig("clientId", clientId);
    }

    public void setClientSecret(String clientSecret) {
        this.setConfig("clientSecret", clientSecret);
    }

    public void setAppHost(String appHost) {
        this.setConfig("appHost", appHost);
    }

    public void setRedirectUri(String redirectUri) {
        this.setConfig("redirectUri", redirectUri);
    }

    private void setConfig(String key, String data) {
        this.config.put(key, data);
    }

    public String loginRequest() {
        init();
        if (this.checkConfig() == null) {
            IDP_Auth_OAuth auth = new IDP_Auth_OAuth();
            String returnString = auth.createAuthUrl(this.config);
            auth = null;
            return returnString;
        }
        return null;
    }

    public String loginRequest2() {
        init();
        if (this.checkConfig() == null) {
            IDP_Auth_OAuth auth = new IDP_Auth_OAuth();
            String returnString = auth.createAuthUrl2(this.config);
            auth = null;
            return returnString;
        }
        return null;
    }

    public String logoutRequest() {
        init();
        if (this.checkConfig() == null) {
            IDP_Auth_OAuth auth = new IDP_Auth_OAuth();
            String returnString = auth.createLogoutUrl(this.config);
            auth = null;
            return returnString;
        }
        return null;
    }

    public String responseRequest(HttpServletRequest request) {
        init();
        IDP_Exception_Message exception = new IDP_Exception_Message();
        if (String.valueOf(this.config.get("clientId")).isEmpty() || String.valueOf(this.config.get("clientId")).length() == 0)
            return exception.missingClientId();
        else if (String.valueOf(this.config.get("clientSecret")).isEmpty() || String.valueOf(this.config.get("clientSecret")).length() == 0)
            return exception.missingClientSecret();

        IDP_Auth_OAuth auth = new IDP_Auth_OAuth();
        String returnString = auth.responseRequest(this.config, request);
        auth = null;
        return returnString;
    }

    private String checkConfig() {
        init();
        IDP_Exception_Message exception = new IDP_Exception_Message();
        if (String.valueOf(this.config.get("clientId")).isEmpty() || String.valueOf(this.config.get("clientId")).length() == 0)
            return exception.missingClientId();
        else if (String.valueOf(this.config.get("clientSecret")).isEmpty() || String.valueOf(this.config.get("clientSecret")).length() == 0)
            return exception.missingClientSecret();
        else if (String.valueOf(this.config.get("redirectUri")).isEmpty() || String.valueOf(this.config.get("redirectUri")).length() == 0)
            return exception.missingRedirectUri();

        return null;
    }
}
