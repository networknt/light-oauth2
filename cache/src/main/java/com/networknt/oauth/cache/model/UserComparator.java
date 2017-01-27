package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by steve on 04/01/17.
 */
public class UserComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        User c1 = (User) o1.getValue();
        User c2 = (User) o2.getValue();
        return c1.getUserId().compareTo(c2.getUserId());
    }
}
