package com.networknt.oauth.cache.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by steve on 04/01/17.
 */
public class ClientComparator implements Comparator<Map.Entry>, Serializable {
    @Override
    public int compare(Map.Entry o1, Map.Entry o2) {
        Client c1 = (Client) o1.getValue();
        Client c2 = (Client) o2.getValue();
        return c1.getClientName().compareTo(c2.getClientName());
    }
}
