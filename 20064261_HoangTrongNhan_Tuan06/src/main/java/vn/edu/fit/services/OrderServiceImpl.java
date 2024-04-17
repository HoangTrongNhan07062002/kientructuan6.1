package vn.edu.fit.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fit.models.Orders;
import vn.edu.fit.repositories.OrderRepository;
import vn.edu.fit.services.impl.IOrderService;

import java.util.Date;

@Service
public class OrderServiceImpl implements IOrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductServiceImpl productService;


    @Override
    public Orders purchaseProduct(Long productId, String name, Integer quantity, double price) {
        Orders order = new Orders();
        order.setId(productId);
        order.setProductID(productId);
        order.setQuantity(quantity);
        order.setCustomerID(1L);
        order.setPrice(price);
        order.setTotalAmount(quantity * price);
        order.setOrderDate(new Date());

        orderRepository.save(order);

        return order;
    }
}
