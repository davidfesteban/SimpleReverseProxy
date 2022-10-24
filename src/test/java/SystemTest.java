import de.naivetardis.service.auth.AuthService;
import de.naivetardis.service.proxy.ProxyService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class SystemTest {

    @Test
    public void givenUserWithAStandardMind() throws IOException, InterruptedException {
        startServer();
        Thread.sleep(2000);
        HttpClient client = HttpClient.newHttpClient();
        client.send(HttpRequest.newBuilder().uri(URI.create("http://firebase.localhost:80")).GET().build(), HttpResponse.BodyHandlers.ofString());
        client.send(HttpRequest.newBuilder().uri(URI.create("http://firebase.localhost:80/auth?code=\"\"&email=\"test@test.com\"&pswd=\"test\"")).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private void startServer() throws InterruptedException {
        AuthService service = new AuthService();
        service.start();
        ProxyService proxyService = new ProxyService();
        proxyService.start();
    }

}
