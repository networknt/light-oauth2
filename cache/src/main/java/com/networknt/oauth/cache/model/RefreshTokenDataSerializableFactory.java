package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

/**
 * Created by stevehu on 2017-01-14.
 */
public class RefreshTokenDataSerializableFactory implements DataSerializableFactory {
    static final int ID = 4;
    static final int REFRESH_TOKEN_TYPE = 4;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == REFRESH_TOKEN_TYPE) {
            return new RefreshToken();
        } else {
            return null;
        }
    }
}
