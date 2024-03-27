package com.grs.core.domain;

import java.util.WeakHashMap;
import java.util.Map;

/**
 * Created by User on 10/19/2017.
 */
public enum RedirectMap {
    PUBLIC_GRIEVANCE_SUBMIT("/addPublicGrievances.do"), STAFF_GRIEVANCE_SUBMIT("/addStaffGrievances.do"), DASHBOARD("/dashboard.do");

    private String redirectUrl;

    RedirectMap(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    private static final WeakHashMap<String, RedirectMap> lookup = new WeakHashMap<>();

    static
    {
        for(RedirectMap redirectMap : RedirectMap.values())
        {
            lookup.put(redirectMap.getRedirectUrl(), redirectMap);
        }
    }

    public static RedirectMap get(String redirectUrl)
    {
        return lookup.get(redirectUrl);
    }
}
