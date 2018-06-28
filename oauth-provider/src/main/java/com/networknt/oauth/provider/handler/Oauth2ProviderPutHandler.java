
package com.networknt.oauth.provider.handler;

import com.hazelcast.core.IMap;
import com.networknt.body.BodyHandler;
import com.networknt.config.Config;
import com.networknt.handler.LightHttpHandler;
import com.networknt.oauth.cache.CacheStartupHookProvider;
import com.networknt.oauth.cache.model.Provider;
import com.networknt.oauth.provider.ProviderAuditHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Oauth2ProviderPutHandler extends ProviderAuditHandler implements LightHttpHandler {
    static Logger logger = LoggerFactory.getLogger(Oauth2ProviderPostHandler.class);
    static final String PROVIDER_ID_EXISTS = "ERR12048";
    static final String PROVIDER_ID_NOT_SET = "ERR12047";
    static final String PROVIDER_ID_INVALID = "ERR12049";
    static final String CONFIG_SECURITY = "security";
    static final String PROVIDER_ID = "providerId";

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        Map<String, Object> body = (Map<String, Object>)exchange.getAttachment(BodyHandler.REQUEST_BODY);
        Provider provider = Config.getInstance().getMapper().convertValue(body, Provider.class);

        // generate provider id from security.yml file
        Map<String, Object> config = Config.getInstance().getJsonMapConfig(CONFIG_SECURITY);
        String provider_id = "" ;
        if (config.get(PROVIDER_ID)==null) {
            setExchangeStatus(exchange, PROVIDER_ID_NOT_SET);
            processAudit(exchange);
            return;
        } else {
            provider_id =config.get(PROVIDER_ID).toString();
            if (provider_id.length() == 1) {
                provider_id = "0" + provider_id;
            } else if (provider_id.length()>2) {
                setExchangeStatus(exchange, PROVIDER_ID_INVALID);
                processAudit(exchange);
                return;
            }
            provider.setProviderId(provider_id);
        }

        IMap<String, Provider> providers = CacheStartupHookProvider.hz.getMap("providers");
        if(providers.get(provider_id) == null) {
            setExchangeStatus(exchange, PROVIDER_ID_INVALID);
        } else {
            providers.set(provider_id, provider);
            exchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(provider));
        }
        processAudit(exchange);
        
    }
}
