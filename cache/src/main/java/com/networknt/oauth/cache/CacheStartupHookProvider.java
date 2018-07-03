package com.networknt.oauth.cache;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.cache.model.*;
import com.networknt.server.StartupHookProvider;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheStartupHookProvider implements StartupHookProvider {
    public static HazelcastInstance hz;
    @Override
    public void onStartup() {
        Config config = new Config();
        config.getNetworkConfig().setPort( 5900 )
                .setPortAutoIncrement( true );

        // data serializer factory
        config.getSerializationConfig().addDataSerializableFactory(1, new UserDataSerializableFactory());
        config.getSerializationConfig().addDataSerializableFactory(2, new ClientDataSerializableFactory());
        config.getSerializationConfig().addDataSerializableFactory(3, new ServiceDataSerializableFactory());
        config.getSerializationConfig().addDataSerializableFactory(4, new RefreshTokenDataSerializableFactory());
        config.getSerializationConfig().addDataSerializableFactory(5, new ServiceEndpointDataSerializableFactory());
        config.getSerializationConfig().addDataSerializableFactory(6, new ProviderDataSerializableFactory());

        // service map with near cache.
        MapConfig serviceConfig = new MapConfig();
        serviceConfig.setName("services");
        NearCacheConfig serviceCacheConfig = new NearCacheConfig();
        serviceCacheConfig.setEvictionPolicy("NONE");
        serviceCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        serviceCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        serviceConfig.setNearCacheConfig(serviceCacheConfig);

        serviceConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ServiceMapStore");

        config.addMapConfig(serviceConfig);

        // service endpoint map with near cache.
        MapConfig serviceEndpointConfig = new MapConfig();
        serviceEndpointConfig.setName("serviceEndpoints");
        NearCacheConfig serviceEndpointCacheConfig = new NearCacheConfig();
        serviceEndpointCacheConfig.setEvictionPolicy("NONE");
        serviceEndpointCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        serviceEndpointCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        serviceEndpointConfig.setNearCacheConfig(serviceEndpointCacheConfig);

        serviceEndpointConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ServiceEndpointMapStore");

        config.addMapConfig(serviceEndpointConfig);

        // client map with near cache.
        MapConfig clientConfig = new MapConfig();
        clientConfig.setName("clients");
        NearCacheConfig clientCacheConfig = new NearCacheConfig();
        clientCacheConfig.setEvictionPolicy("NONE");
        clientCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        clientCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        clientConfig.setNearCacheConfig(clientCacheConfig);

        clientConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ClientMapStore");

        config.addMapConfig(clientConfig);

        // code map with near cache and evict.
        MapConfig codeConfig = new MapConfig();
        codeConfig.setName("codes");
        NearCacheConfig codeCacheConfig = new NearCacheConfig();
        codeCacheConfig.setTimeToLiveSeconds(60 * 60 * 1000); // 1 hour TTL
        codeCacheConfig.setMaxIdleSeconds(10 * 60 * 1000);    // 10 minutes max idle seconds
        codeCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        codeCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        codeConfig.setNearCacheConfig(codeCacheConfig);
        config.addMapConfig(codeConfig);

        // fresh token map with near cache and evict. A new refresh token will
        // be generated each time refresh token is used. This token only lives
        // for 1 day and it will be removed from the cache automatically.
        MapConfig tokenConfig = new MapConfig();
        tokenConfig.setName("tokens");
        NearCacheConfig tokenCacheConfig = new NearCacheConfig();
        /*
        tokenCacheConfig.setTimeToLiveSeconds(24 * 60 * 60 * 1000); // 1 hour TTL
        tokenCacheConfig.setMaxIdleSeconds(24 * 60 * 60 * 1000);    // 30 minutes max idle seconds
        tokenCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        tokenCacheConfig.setCacheLocalEntries(true); // this enables the local
        */

        tokenConfig.setNearCacheConfig(tokenCacheConfig);

        tokenConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.RefreshTokenMapStore");

        config.addMapConfig(tokenConfig);

        // user map distributed.
        MapConfig userConfig = new MapConfig();
        userConfig.setName("users");
        userConfig.setBackupCount(1);
        userConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.UserMapStore");
        userConfig.addMapIndexConfig(new MapIndexConfig("email", true));

        config.addMapConfig(userConfig);

        // provider map distributed.
        MapConfig providerConfig = new MapConfig();
        providerConfig.setName("providers");
        NearCacheConfig providerCacheConfig = new NearCacheConfig();
        providerCacheConfig.setEvictionPolicy("NONE");
        providerCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        providerCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        providerConfig.setNearCacheConfig(providerCacheConfig);
        providerConfig.getMapStoreConfig()
                .setEnabled(true)
                .setClassName("com.networknt.oauth.cache.ProviderMapStore");

        config.addMapConfig(providerConfig);

        hz = Hazelcast.newHazelcastInstance( config );

    }
}
