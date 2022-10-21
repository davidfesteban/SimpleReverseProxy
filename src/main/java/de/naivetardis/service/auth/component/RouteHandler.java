package de.naivetardis.service.auth.component;

import com.sun.net.httpserver.HttpExchange;
import de.naivetardis.service.utils.PropertiesContext;
import de.naivetardis.service.utils.RandomString;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class RouteHandler extends BasicHandler {
    private final RandomString randomString;
    private final Properties context;

    public RouteHandler() {
        super();
        context = PropertiesContext.getInstance().getContext();
        randomString = new RandomString(Integer.parseInt(context.getProperty("random.length")));
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (isAuthSuccessfull(t)) {
            addToken(t);
            redirect(t);
        } else {
            authView(t);
        }
    }

    private void addToken(HttpExchange exchange) {
        String token = randomString.nextString();
        security().put(token, new Date());
        exchange.getResponseHeaders().add(context("header.cookie.set"), context("header.token") + "=" + token);
    }

    private boolean isAuthSuccessfull(HttpExchange exchange) {
        Map<String, String> queryParams = queryToMap(exchange.getRequestURI().getQuery());

        if (queryParams != null) {
            return queryParams.getOrDefault("email", "").equalsIgnoreCase(context("security.email"))
                    && queryParams.getOrDefault("pswd", "").equalsIgnoreCase(context("security.pswd"));
        }

        return false;
    }
}
