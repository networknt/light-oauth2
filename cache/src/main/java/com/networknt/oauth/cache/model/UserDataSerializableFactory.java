package com.networknt.oauth.cache.model;

import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

/**
 * Created by stevehu on 2017-01-02.
 */
public class UserDataSerializableFactory implements DataSerializableFactory {

    static final int ID = 1;
    static final int USER_TYPE = 1;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == USER_TYPE) {
            return new User();
        } else {
            return null;
        }
    }
}
