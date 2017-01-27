package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

/**
 * Created by stevehu on 2017-01-02.
 */
public class ServiceDataSerializableFactory implements DataSerializableFactory {

    static final int ID = 3;
    static final int SERVICE_TYPE = 3;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == SERVICE_TYPE) {
            return new Service();
        } else {
            return null;
        }
    }
}
