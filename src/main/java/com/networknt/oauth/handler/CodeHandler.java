package com.networknt.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CodeHandler implements HttpHandler {
    static final Logger logger = LoggerFactory.getLogger(CodeHandler.class);

    // this singleton map contains all the auth codes generated. if the code is
    // used in TokenHandler, then it is removed.
    public static Map<String, Object> codes = new HashMap<String, Object>();

    private final ObjectMapper objectMapper;

    public CodeHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(
                Headers.CONTENT_TYPE, "application/json");

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        Map<String, Object> formMap = new HashMap<String, Object>();

        final FormParserFactory parserFactory = FormParserFactory.builder().build();
        final FormDataParser parser = parserFactory.createParser(exchange);
        try {
            FormData data = parser.parseBlocking();
            Iterator<String> it = data.iterator();
            while(it.hasNext()) {
                String fd = it.next();
                for (FormData.FormValue val : data.get(fd)) {
                    logger.debug("fd = " + fd + " value = " + val.getValue());
                    formMap.put(fd, val.getValue());
                }
            }
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            exchange.getResponseSender().send(ByteBuffer.wrap(
                    objectMapper.writeValueAsBytes(
                            Collections.singletonMap("error", "Unable to parse form data"))));
        }
        



        exchange.getResponseSender().send(ByteBuffer.wrap(
                objectMapper.writeValueAsBytes(
                        Collections.singletonMap("message", "Hello World"))));
    }
}
