package com.networknt.oauth.code.auth;

import com.networknt.exception.ApiException;
import io.undertow.server.HttpServerExchange;

/**
 * Created by stevehu on 2016-12-18.
 */
public interface Authentication {
    String authenticate(HttpServerExchange exchange) throws ApiException;
}
