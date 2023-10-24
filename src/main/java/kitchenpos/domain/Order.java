package kitchenpos.domain;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static kitchenpos.domain.OrderStatus.COOKING;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_table_id")
    private OrderTable orderTable;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private LocalDateTime orderedTime;

    @Embedded
    private OrderLineItems orderLineItems = new OrderLineItems();

    protected Order() {
    }

    public Order(final OrderTable orderTable,
                 final List<OrderLineItem> orderLineItems) {
        this.orderTable = orderTable;
        orderTable.placeOrder(this);
        this.orderStatus = COOKING;
        this.orderedTime = LocalDateTime.now();
        orderLineItems.forEach(orderLineItem -> orderLineItem.setOrder(this));
        this.orderLineItems = new OrderLineItems(orderLineItems);
    }

    public void changeOrderStatus(final OrderStatus orderStatus) {
        if (!orderStatus.isInProgress()) {
            throw new IllegalArgumentException("조리가 끝난 주문은 상태를 변경할 수 없습니다.");
        }
        this.orderStatus = orderStatus;
    }

    public boolean isInProgress() {
        return orderStatus.isInProgress();
    }

    public Long getId() {
        return id;
    }

    public OrderTable getOrderTable() {
        return orderTable;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public OrderLineItems getOrderLineItems() {
        return orderLineItems;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
