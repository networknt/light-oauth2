package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by stevehu on 2017-01-14.
 */
public class RefreshTokenComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        RefreshToken c1 = (RefreshToken) o1.getValue();
        RefreshToken c2 = (RefreshToken) o2.getValue();
        return c1.getUserId().compareTo(c2.getUserId());
    }
}
