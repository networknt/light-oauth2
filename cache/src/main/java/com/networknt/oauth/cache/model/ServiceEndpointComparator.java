package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class ServiceEndpointComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        ServiceEndpoint c1 = (ServiceEndpoint) o1.getValue();
        ServiceEndpoint c2 = (ServiceEndpoint) o2.getValue();
        return c1.getEndpoint().compareTo(c2.getEndpoint());
    }
}
