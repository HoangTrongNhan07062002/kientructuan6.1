package vn.edu.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fit.models.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {

}
