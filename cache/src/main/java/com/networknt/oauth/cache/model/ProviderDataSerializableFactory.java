package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ProviderDataSerializableFactory implements DataSerializableFactory {

    static final int ID = 2;
    static final int PROVIDER_TYPE = 2;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == PROVIDER_TYPE) {
            return new Provider();
        } else {
            return null;
        }
    }
}
