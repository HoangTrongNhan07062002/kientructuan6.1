package vn.edu.fit.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long customerID;
    private long productID;
    private int quantity;
    private double price;
    private double totalAmount;
    private Date orderDate;

    public Orders(long customerID, long productID, int quantity, double price, double totalAmount, Date orderDate) {
        this.customerID = customerID;
        this.productID = productID;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }
}
