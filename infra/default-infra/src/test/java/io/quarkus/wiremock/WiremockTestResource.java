package io.quarkus.wiremock;

import java.util.Map;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class WiremockTestResource implements QuarkusTestResourceLifecycleManager {
    public static WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        WireMockConfiguration config = new WireMockConfiguration()
                .usingFilesUnderDirectory("src/test/resources")
                .notifier(new ConsoleNotifier(true))
                .port(9180);
        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        return Map.of("quarkus.rest-client.product-store.url", "http://localhost:9180/product-store/api");
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
