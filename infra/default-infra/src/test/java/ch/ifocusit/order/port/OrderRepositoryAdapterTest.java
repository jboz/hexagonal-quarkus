package ch.ifocusit.order.port;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import ch.ifocusit.order.infra.entity.OrderEntity;
import ch.ifocusit.order.infra.port.OrderRepositoryAdapter;
import ch.ifocusit.order.model.Order;
import ch.ifocusit.order.model.OrderStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.vertx.RunOnVertxContext;
import io.quarkus.test.vertx.UniAsserter;
import jakarta.inject.Inject;

@QuarkusTest
public class OrderRepositoryAdapterTest {

    @Inject
    OrderRepositoryAdapter adapter;

    @Test
    @RunOnVertxContext
    void streamAll(UniAsserter asserter) {
        // given
        asserter.execute(() -> OrderEntity.deleteAll());
        asserter.execute(() -> createEntity("bottes", 10).persist());
        asserter.execute(() -> createEntity("chaussettes", 20).persist());
    }

    @Test
    @RunOnVertxContext
    void insert(UniAsserter asserter) {
        // given
        asserter.execute(() -> OrderEntity.deleteAll());
        var newOrder = Order.builder()
                .productId("chaussettes")
                .quantity(1000)
                .status(OrderStatus.NEW)
                .build();

        // when
        asserter.execute(() -> adapter.persist(newOrder));

        // then
        asserter.assertThat(
                () -> OrderEntity.<OrderEntity>findAll().list(),
                orders -> {
                    assertThat(orders)
                            .hasSize(1)
                            .first()
                            .satisfies(order -> {
                                assertThat(order.id).isNotNull();
                                assertThat(order.status).isEqualTo(newOrder.getStatus());
                                assertThat(order.productId).isEqualTo(newOrder.getProductId());
                                assertThat(order.quantity).isEqualTo(newOrder.getQuantity());
                            });
                });
    }

    @Test
    @RunOnVertxContext
    void update(UniAsserter asserter) {
        // given
        asserter.execute(() -> OrderEntity.deleteAll());
        var entity = createEntity("bottes", 10);
        asserter.execute(() -> entity.persist());

        // when
        asserter.execute(() -> adapter.persist(entity.toDomain().toBuilder().quantity(10).build()));

        // then
        asserter.assertThat(
                () -> OrderEntity.<OrderEntity>findAll().list(),
                orders -> {
                    assertThat(orders)
                            .hasSize(1)
                            .first()
                            .satisfies(order -> {
                                assertThat(order.id).isEqualTo(entity.id);
                                assertThat(order.status).isEqualTo(entity.status);
                                assertThat(order.productId).isEqualTo(entity.productId);
                                assertThat(order.quantity).isEqualTo(entity.quantity);
                            });
                });
    }

    private OrderEntity createEntity(String productId, int quantity) {
        var entity = new OrderEntity();
        entity.setProductId(productId);
        entity.setQuantity(quantity);
        entity.setStatus(OrderStatus.NEW);
        return entity;
    }
}
