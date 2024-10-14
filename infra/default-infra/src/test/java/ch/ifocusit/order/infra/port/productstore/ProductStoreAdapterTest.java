package ch.ifocusit.order.infra.port.productstore;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.Test;
import ch.ifocusit.quarkus.wiremock.WiremockTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(WiremockTestResource.class)
public class ProductStoreAdapterTest {

    @Inject
    private ProductStoreAdapter adapter;

    @Test
    void testAvailable() {
        // when
        adapter.ifAvailable("chaussettes", 5)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertCompleted();

        WiremockTestResource.wireMockServer
                .verify(1, anyRequestedFor(urlEqualTo("/product-store/api/available?productId=chaussettes&quantity=5")));
    }

    @Test
    void testNotAvailable() {
        // when
        adapter.ifAvailable("bottes", 10)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                // then
                .assertFailed();

        WiremockTestResource.wireMockServer
                .verify(1, anyRequestedFor(urlEqualTo("/product-store/api/available?productId=bottes&quantity=10")));
    }
}
