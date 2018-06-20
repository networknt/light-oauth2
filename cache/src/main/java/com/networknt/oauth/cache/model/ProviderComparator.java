package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Comparator
 */
public class ProviderComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        Provider c1 = (Provider) o1.getValue();
        Provider c2 = (Provider) o2.getValue();
        return c1.getProviderId().compareTo(c2.getProviderId());
    }
}
