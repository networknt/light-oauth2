package com.networknt.oauth.cache;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.networknt.oauth.cache.model.*;
import com.networknt.server.StartupHookProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheStartupHookProvider implements StartupHookProvider {
    static final Logger logger = LoggerFactory.getLogger(CacheStartupHookProvider.class);
    public static HazelcastInstance hz;
    public static final String CONFIG_NAME = "hazelcast.xml";

    @Override
    public void onStartup() {
        InputStream is = com.networknt.config.Config.getInstance().getInputStreamFromFile(CONFIG_NAME);
        Config config = null;
        if(is != null) {
            logger.info("customized hazelcast.xml is loaded from config.");
            config = new XmlConfigBuilder(is).build();
        } else {
            logger.info("default hazelcast.xml is loaded.");
            config = new Config();
        }

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
        serviceCacheConfig.getEvictionConfig();
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
        serviceEndpointCacheConfig.getEvictionConfig();
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
        clientCacheConfig.getEvictionConfig();
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

        // reference token to jwt mapping
        MapConfig referenceConfig = new MapConfig();
        referenceConfig.setName("references");
        NearCacheConfig referenceCacheConfig = new NearCacheConfig();
        referenceCacheConfig.setTimeToLiveSeconds(60 * 60 * 1000); // 1 hour TTL
        referenceCacheConfig.setMaxIdleSeconds(10 * 60 * 1000);    // 10 minutes max idle seconds
        referenceCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
        referenceCacheConfig.setCacheLocalEntries(true); // this enables the local caching
        referenceConfig.setNearCacheConfig(referenceCacheConfig);
        config.addMapConfig(referenceConfig);

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
        userConfig.addIndexConfig(new IndexConfig(IndexType.SORTED, "email"));

        config.addMapConfig(userConfig);

        // provider map distributed.
        MapConfig providerConfig = new MapConfig();
        providerConfig.setName("providers");
        NearCacheConfig providerCacheConfig = new NearCacheConfig();
        providerCacheConfig.getEvictionConfig();
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
