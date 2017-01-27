package com.networknt.oauth.cache;

import com.networknt.server.ShutdownHookProvider;

/**
 * Created by stevehu on 2016-12-27.
 */
public class CacheShutdownHookProvider implements ShutdownHookProvider {
    @Override
    public void onShutdown() {
        CacheStartupHookProvider.hz.shutdown();
    }
}
