package com.drebo.microservices.order.service;

import com.drebo.microservices.order.client.InventoryClient;
import com.drebo.microservices.order.domain.dto.OrderDto;
import com.drebo.microservices.order.domain.dto.OrderListDto;
import com.drebo.microservices.order.domain.entity.Order;
import com.drebo.microservices.order.mapper.OrderMapper;
import com.drebo.microservices.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, InventoryClient inventoryClient ){
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.inventoryClient = inventoryClient;
    }

    public void placeOrder(OrderDto orderDto) {

        boolean inStock = inventoryClient.inStock(orderDto.getSku(), orderDto.getQuantity());

        if (inStock) {
            Order order = orderMapper.mapFrom(orderDto);
            Order savedOrder = orderRepository.save(order);
            log.info("Order number: {} placed successfully", savedOrder.getOrderNumber());
        } else {
            throw new RuntimeException("Product with sku code " + orderDto.getSku() + "not in stock.");
        }
    }

    public OrderListDto getAllOrders(){
        List<Order> allOrders = orderRepository.findAll();
        List<OrderDto> allOrdersDto = allOrders.stream().map(order -> orderMapper.mapTo(order)).toList();

        return new OrderListDto(allOrdersDto);
    }

    public void deleteAllOrders(){
        orderRepository.deleteAll();
        log.info("All orders deleted.");
    }
}
