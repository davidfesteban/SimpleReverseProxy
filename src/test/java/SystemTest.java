import de.naivetardis.service.auth.AuthService;
import de.naivetardis.service.proxy.ProxyService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SystemTest {

    @Test
    public void givenUserWithAStandardMind() throws IOException, InterruptedException {
        startServer();
        HttpClient client = HttpClient.newHttpClient();
        client.send(HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:80")).GET().build(), HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder().uri(URI.create("http://serv.localhost:80/auth?code=\"\"&email=\"test@test.com\"&pswd=\"test\"")).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private void startServer() {
        new AuthService().start();
        new ProxyService().start();
    }

}
