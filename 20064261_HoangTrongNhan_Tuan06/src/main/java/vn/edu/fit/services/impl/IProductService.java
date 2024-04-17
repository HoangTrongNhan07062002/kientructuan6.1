package vn.edu.fit.services.impl;

import vn.edu.fit.models.Product;

import java.util.List;

public interface IProductService {
    String getProductName(long ID);
    List<Product> getAllProducts();
    int getProductQuantity(Long productId);
}
