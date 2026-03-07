package com.shailesh.order.controller;

import com.shailesh.order.entity.Order;
import com.shailesh.order.messaging.OrderEventProducer;
import com.shailesh.order.repository.OrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Order Controller
 *
 * REST endpoints for order management.
 * All order state changes trigger Kafka events.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    public OrderController(OrderRepository orderRepository, OrderEventProducer eventProducer) {
        this.orderRepository = orderRepository;
        this.eventProducer = eventProducer;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(o -> ResponseEntity.ok(toDto(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(orders.stream().map(this::toDto).toList());
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = Order.builder()
                    .userId(request.userId())
                    .status("PENDING")
                    .totalAmount(request.totalAmount())
                    .build();

            Order saved = orderRepository.save(order);
            log.info("Order created: {} for user {}", saved.getId(), saved.getUserId());

            // Publish CREATE event to Kafka
            // Key: userId (ensures ordering per user)
            eventProducer.publishOrderEvent(
                    new OrderEventProducer.OrderEvent(
                            saved.getId(),
                            saved.getUserId(),
                            "CREATE",
                            saved.getStatus(),
                            saved.getTotalAmount(),
                            saved.getCreatedAt()
                    )
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            order.setStatus(status);
            Order updated = orderRepository.save(order);
            log.info("Order {} status updated to: {}", id, status);

            // Publish UPDATE event to Kafka
            eventProducer.publishOrderEvent(
                    new OrderEventProducer.OrderEvent(
                            updated.getId(),
                            updated.getUserId(),
                            "UPDATE",
                            updated.getStatus(),
                            updated.getTotalAmount(),
                            updated.getUpdatedAt()
                    )
            );

            return ResponseEntity.ok(toDto(updated));
        } catch (Exception e) {
            log.error("Failed to update order: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId().toString(),
                order.getUserId().toString(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    // DTOs
    public record OrderDto(
            String id,
            String userId,
            String status,
            java.math.BigDecimal totalAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record CreateOrderRequest(
            @NotNull Long userId,
            @NotNull @Min(0) java.math.BigDecimal totalAmount
    ) {}
}
