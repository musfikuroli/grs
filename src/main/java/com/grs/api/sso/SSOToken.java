package com.grs.api.sso;

public class SSOToken {
    private SSOPropertyReader ssoPropertyReader;

    public SSOToken() throws Exception{
        this.ssoPropertyReader = SSOPropertyReader.getInstance();
    }
    public long getExpiryTime(){
        long curUtc = System.currentTimeMillis();
        return curUtc + Long.parseLong(this.ssoPropertyReader.getEtIntervalms());
    }
}
