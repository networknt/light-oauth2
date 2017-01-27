package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

/**
 * Created by stevehu on 2017-01-02.
 */
public class ClientDataSerializableFactory implements DataSerializableFactory {

    static final int ID = 2;
    static final int CLIENT_TYPE = 2;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == CLIENT_TYPE) {
            return new Client();
        } else {
            return null;
        }
    }
}
