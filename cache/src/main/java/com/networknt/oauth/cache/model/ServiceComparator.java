package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by steve on 04/01/17.
 */
public class ServiceComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        Service c1 = (Service) o1.getValue();
        Service c2 = (Service) o2.getValue();
        return c1.getServiceId().compareTo(c2.getServiceId());
    }
}
