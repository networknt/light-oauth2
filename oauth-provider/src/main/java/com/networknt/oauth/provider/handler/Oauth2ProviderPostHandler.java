package com.networknt.oauth.provider.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.Provider;
import com.networknt.oauth.cache.model.User;
import com.networknt.oauth.provider.ProviderAuditHandler;
import com.networknt.utility.HashUtil;
import com.networknt.utility.Util;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class Oauth2ProviderPostHandler extends ProviderAuditHandler implements LightHttpHandler {

    static Logger logger = LoggerFactory.getLogger(Oauth2ProviderPostHandler.class);
    static final String PROVIDER_ID_EXISTS = "ERR12019";
    static final String USER_NOT_FOUND = "ERR12013";

    @SuppressWarnings("unchecked")
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Provider provider = Config.getInstance().getMapper().convertValue(body, Provider.class);

        // generate provider id here

        String providerId = "01";  //todo get the next provider Id
        provider.setProviderId(providerId);


        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");
        if(providers.get(providerId) == null) {
            providers.set(providerId, provider);
            // send the client back with client_id and client_secret

            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(provider));
        } else {
            setExchangeStatus(exchange, PROVIDER_ID_EXISTS, providerId);
        }
        processAudit(exchange);
    }
}
