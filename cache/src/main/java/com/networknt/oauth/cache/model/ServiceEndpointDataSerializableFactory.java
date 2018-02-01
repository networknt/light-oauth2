package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ServiceEndpointDataSerializableFactory implements DataSerializableFactory {

    static final int ID = 5;
    static final int SERVICE_ENDPOINT_TYPE = 5;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == SERVICE_ENDPOINT_TYPE) {
            return new ServiceEndpoint();
        } else {
            return null;
        }
    }
}
