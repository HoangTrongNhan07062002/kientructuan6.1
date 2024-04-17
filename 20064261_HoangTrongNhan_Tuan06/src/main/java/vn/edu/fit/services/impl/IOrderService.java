package vn.edu.fit.services.impl;

import vn.edu.fit.models.Orders;

public interface IOrderService {
    Orders purchaseProduct(Long productId, String name, Integer quantity, double price);
}
