package com.networknt.oauth.provider.handler;

import com.hazelcast.map.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Provider;
import com.networknt.oauth.provider.ProviderAuditHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class Oauth2ProviderPostHandler extends ProviderAuditHandler implements LightHttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2ProviderPostHandler.class);
    static final String PROVIDER_ID_EXISTS = "ERR12048";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Provider provider = Config.getInstance().getMapper().convertValue(body, Provider.class);

        String provider_id = provider.getProviderId() ;

        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");
        if(providers.get(provider_id) == null) {
            providers.set(provider_id, provider);
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(provider));
        } else {
            setExchangeStatus(exchange, PROVIDER_ID_EXISTS, provider_id);
        }
        processAudit(exchange);
    }
}


