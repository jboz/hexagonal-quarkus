package ch.ifocusit.order.infra.port.productstore;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;

@QuarkusTest
@ConnectWireMock
public class ProductStoreAdapterTest {

    WireMock wireMock;

    @Inject
    private ProductStoreAdapter adapter;

    @Test
    void testAvailable() {
        // given a manual rest call registration
        // wireMock.register(get(urlEqualTo("/product-store/api/available?productId=chaussettes&quantity=5"))
        // .willReturn(aResponse().withStatus(201)));

        // when
        adapter.ifAvailable("chaussettes", 5)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .awaitItem()
                .assertCompleted();

        wireMock
                .verifyThat(1, anyRequestedFor(urlEqualTo("/product-store/api/available?productId=chaussettes&quantity=5")));
    }

    @Test
    void testNotAvailable() {
        // when
        adapter.ifAvailable("bottes", 10)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .awaitFailure()
                .assertFailed();

        wireMock
                .verifyThat(1, anyRequestedFor(urlEqualTo("/product-store/api/available?productId=bottes&quantity=10")));
    }
}
