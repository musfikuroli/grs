package com.grs.api.sso;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class SSOPropertyReader {
    private static final String ssoPropertiesFileName = "sso.properties";
    private static SSOPropertyReader ssoFileReader = null;
    private static String appName = "";
    private static String appId = "";
    private static String secret = "";
    private static String idpUrl = "";
    private static String appNameQS = "";
    private static String appLoginEndPoint = "";
    private static String iaLoginEndPoint = "";
    private static String etIntervalms = "";
    private static String redirectUri = "";
    private static String landingPageUri = "";
    private static String loginPageUri = "";
    private static String baseUri = "";
    private static String myGovApiBaseUri = "";
    private static String myGovApiState = "";
    private static String myGovComplaintApiBaseUri = "";
    private static String mygovClientId = "";
    private static String mygovClientSecret = "";
    private static String mygovApiSecret = "";
    private static String mygovComplainUser = "";
    private static String mygovComplainSecret = "";
    private static String myGovRedirectUrl = "";

    private static String activeProfile;

    private SSOPropertyReader() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        InputStream resource = classLoader.getResourceAsStream(ssoPropertiesFileName);
        Properties applicationProperties = new Properties();
        applicationProperties.load(classLoader.getResourceAsStream("application.properties"));
        activeProfile = applicationProperties.getProperty("spring.profiles.active");
        activeProfile = activeProfile != null ? activeProfile : "dev";


        Path temp = Files.createTempFile("sso-", ".properties");
//        Files.copy(classLoader.getResourceAsStream("sso.properties"), temp, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(classLoader.getResourceAsStream("sso-" + activeProfile + ".properties"), temp, StandardCopyOption.REPLACE_EXISTING);

        FileInputStream fileInput = null;
        try {
            fileInput = new FileInputStream(temp.toFile());
            Properties properties = new Properties();
            properties.load(fileInput);
            fileInput.close();
            appId = properties.getProperty("appId");
            appName = properties.getProperty("appName");
            secret = properties.getProperty("sharedSecret");
            idpUrl = properties.getProperty("idpurl");
            appNameQS = properties.getProperty("appNameQS");
            appLoginEndPoint = properties.getProperty("apploginendpoint");
            iaLoginEndPoint = properties.getProperty("ialoginendpoint");
            etIntervalms = properties.getProperty("etintervalms");
            redirectUri = properties.getProperty("redirectUri");
            landingPageUri = properties.getProperty("landingPageUri");
            loginPageUri = properties.getProperty("loginpageuri");
            baseUri = properties.getProperty("baseUri");
            myGovApiBaseUri = properties.getProperty("myGovApiBaseUri");
            myGovApiState = properties.getProperty("myGovApiState");
            myGovComplaintApiBaseUri = properties.getProperty("myGovComplaintApiBaseUri");
            mygovClientId = properties.getProperty("mygovClientId");
            mygovClientSecret = properties.getProperty("mygovClientSecret");
            mygovApiSecret = properties.getProperty("mygovApiSecret");
            mygovComplainUser = properties.getProperty("mygovComplainUser");
            mygovComplainSecret = properties.getProperty("mygovComplainSecret");
            myGovRedirectUrl = properties.getProperty("myGovRedirectUrl");
        } finally {
            if (fileInput != null) {
                fileInput.close();
            }
        }

        if (appId == null || appId.equals("")) {
            throw new Exception("App id is null or empty. Could not read app id from property file. Please set app id in properties file");
        }

        if (appName == null || appName.equals("")) {
            throw new Exception("App name is null or empty. Could not read app name from property file. Please set ap pname in properties file.");
        }

        if (secret == null || secret.equals("")) {
            throw new Exception("App id is null or empty. Could not read app id from property file. Please set app id in properties file");
        }

        if (idpUrl == null || idpUrl.equals("")) {
            throw new Exception("App id is null or empty. Could not read app id from property file. Please set app id in properties file");
        }

        if (appNameQS == null || appNameQS.equals("")) {
            throw new Exception("App id is null or empty. Could not read app id from property file. Please set app id in properties file");
        }

        if (appLoginEndPoint == null || appLoginEndPoint.equals("")) {
            throw new Exception("App login end point is null or empty. Could not read app id from property file. Please set app id in properties file");
        }

        if (iaLoginEndPoint == null || iaLoginEndPoint.equals("")) {
            throw new Exception("Inter app login end point is null or empty. Could not read dashboardLoginEndPoint from property file. Please set up dashboardLoginEndPoint in properties file");
        }

        if (this.etIntervalms == null || etIntervalms.equals("")) {
            throw new Exception("Expiry time interval is null or empty. Could not read etIntervalms from property file. Please set up etIntervalms in properties file");
        }

        if (loginPageUri == null || loginPageUri.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read loginpageuri from property file. Please set up loginpageuri in properties file");
        }

        if (baseUri == null || baseUri.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read baseUri from property file. Please set up baseUri in properties file");
        }

        if (myGovApiBaseUri == null || myGovApiBaseUri.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read myGovApiBaseUri from property file. Please set up myGovApiBaseUri in properties file");
        }
        if (myGovApiState == null || myGovApiState.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read myGovApiState from property file. Please set up myGovApiState in properties file");
        }

        if (myGovComplaintApiBaseUri == null || myGovComplaintApiBaseUri.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read myGovComplaintApiBaseUri from property file. Please set up myGovComplaintApiBaseUri in properties file");
        }

        if (mygovClientId == null || mygovClientId.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read mygovClientId from property file. Please set up mygovClientId in properties file");
        }

        if (mygovClientSecret == null || mygovClientSecret.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read mygovClientSecret from property file. Please set up mygovClientSecret in properties file");
        }

        if (mygovApiSecret == null || mygovApiSecret.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read mygovApiSecret from property file. Please set up mygovApiSecret in properties file");
        }

        if (mygovComplainUser == null || mygovComplainUser.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read mygovComplainUser from property file. Please set up mygovComplainUser in properties file");
        }

        if (mygovComplainSecret == null || mygovComplainSecret.equals("")) {
            throw new Exception("Login page uri is null or empty. Could not read mygovComplainSecret from property file. Please set up mygovComplainSecret in properties file");
        }
        if (myGovRedirectUrl == null || myGovRedirectUrl.equals("")) {
            throw new Exception("myGov Redirect Url is null or empty. Could not read myGovRedirectUrl from property file. Please set up myGovRedirectUrl in properties file");
        }
    }

    private synchronized static void createSSOFileReader() throws Exception {
        if (ssoFileReader == null) {
            ssoFileReader = new SSOPropertyReader();
        }
    }

    public synchronized static SSOPropertyReader getInstance() throws Exception {
        if (ssoFileReader == null) {
            createSSOFileReader();
        }
        return ssoFileReader;
    }

    public String getMyGovApiState() {
        return myGovApiState;
    }

    public String getAppId() {
        return appId;
    }

    public String getAppName() {
        return appName;
    }

    public String getSecret() {
        return secret;
    }

    public String getIdpUrl() {
        return idpUrl;
    }

    public String getAppNameQS() {
        return appNameQS;
    }

    public String getAppLoginEndPoint() {
        return appLoginEndPoint;
    }

    public String getIALoginEndPoint() {
        return iaLoginEndPoint;
    }

    public String getEtIntervalms() {
        return etIntervalms;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public String getLandingPageUri() {
        return landingPageUri;
    }

    public String getLoginPageUri() {
        return loginPageUri;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String getMyGovApiBaseUri() {
        return myGovApiBaseUri;
    }

    public String getMyGovComplaintApiBaseUri() {
        return myGovComplaintApiBaseUri;
    }

    public String getMygovClientId() {
        return mygovClientId;
    }

    public String getMygovClientSecret() {
        return mygovClientSecret;
    }

    public String getMygovApiSecret() {
        return mygovApiSecret;
    }

    public String getMygovComplainUser() {
        return mygovComplainUser;
    }

    public String getMygovComplainSecret() {
        return mygovComplainSecret;
    }

    public String getMyGovRedirectUrl() {
        return myGovRedirectUrl;
    }
}
