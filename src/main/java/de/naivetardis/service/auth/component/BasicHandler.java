package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.naivetardis.service.utils.PropertiesContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

abstract class BasicHandler implements HttpHandler {
    private static final Map<String, Date> loggedTokens = new HashMap<>();
    private final Properties context;

    public BasicHandler() {
        this.context = PropertiesContext.getInstance().getContext();
    }

    void redirect(HttpExchange exchange) throws IOException {
        String response = Files.readString(Path.of("src/main/resources/web/redirect.html"));
        response = response.replace("{}", context.getProperty("ip.localhost").replace("localhost", exchange.getRequestHeaders().get("Host").get(0)) + context.getProperty("proxy.port.external"));
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    void authView(HttpExchange exchange) throws IOException {
        String response = Files.readString(Path.of("src/main/resources/web/login.html"));
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }

    Map<String, String> queryToMap(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    String context(String key) {
        return context.getProperty(key);
    }

    Map<String, Date> security() {
        return loggedTokens;
    }
}
