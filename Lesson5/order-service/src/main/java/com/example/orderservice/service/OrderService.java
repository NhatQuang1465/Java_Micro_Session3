package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO getOrderResponseById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponseDTO(order);
    }

    public List<OrderResponseDTO> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        if (requestDTO.getQuantity() == null || requestDTO.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        // Giả lập giá sản phẩm lấy từ Product Service (ví dụ 100.00 đơn vị tiền tệ)
        BigDecimal unitPrice = getSimulatedProductPrice(requestDTO.getProductId());
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(requestDTO.getQuantity()));

        Order order = new Order();
        order.setCustomerId(requestDTO.getCustomerId());
        order.setProductId(requestDTO.getProductId());
        order.setQuantity(requestDTO.getQuantity());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");

        Order savedOrder = orderRepository.save(order);
        return mapToResponseDTO(savedOrder);
    }

    private BigDecimal getSimulatedProductPrice(Long productId) {
        // Giả lập lấy giá dựa trên productId (mặc định 100.0 cho mọi sản phẩm hoặc tính sơ bộ)
        return BigDecimal.valueOf(100.0);
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }
}

