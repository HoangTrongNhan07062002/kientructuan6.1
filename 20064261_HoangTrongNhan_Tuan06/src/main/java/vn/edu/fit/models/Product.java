package vn.edu.fit.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String productName;
    private String description;
    private String imagePath;
    private int quantity;
    private double price;

    public Product(String productName, String description, String imagePath, int quantity, Double price) {
        this.productName = productName;
        this.description = description;
        this.imagePath = imagePath;
        this.quantity = quantity;
        this.price = price;
    }
}
