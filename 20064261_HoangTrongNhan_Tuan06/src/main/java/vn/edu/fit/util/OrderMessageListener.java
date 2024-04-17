package vn.edu.fit.util;

import com.google.gson.Gson;
import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import org.apache.activemq.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import vn.edu.fit.dto.OrderDTO;
import vn.edu.fit.models.Orders;
import vn.edu.fit.services.OrderServiceImpl;
import vn.edu.fit.services.ProductServiceImpl;

@Component
public class OrderMessageListener {
    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ProductServiceImpl productService;


    @JmsListener(destination = "order_products")
    public void receiveMessage(final Message jsonMessage) throws JMSException {
        String messageData = null;
        String response = null;
        System.out.println("...Đơn hàng đã nhận..");

        int flag = 0;

        if (jsonMessage instanceof TextMessage textMessage) {
            messageData = textMessage.getText();

            // Decode message data
            String decodedString = Decryptor.decryptBase64ToJson(messageData);
            Gson gson = new Gson();
            OrderDTO order = gson.fromJson(decodedString, OrderDTO.class);

            // Check for quantity
            boolean purchaseResult = checkProductAvailability(order.getProductId(), order.getQuantity());

            Orders orders = new Orders();

            // Make order or reject
            if (purchaseResult) {
                orders = orderService.purchaseProduct(order.getProductId(), order.getName(), order.getQuantity(), order.getPricePerUnit());
                response = "Đặt hàng thành công";
                flag = 1;
            } else {
                response = "Đơn hàng bị từ chối vì không đủ số lượng trong kho";
                flag = 0;
            }

            // Send email
            String recipientEmail = "tuthanh375@gmail.com";
            String subject = "Thông báo đơn hàng";
            String body = "";

            StringBuilder orderDetails = new StringBuilder();
            orderDetails.append("Chi tiết đơn hàng:\n");
            orderDetails.append("-------------------------------------\n");
            orderDetails.append("   Tên sản phẩm: ").append(productService.getProductName(order.getProductId())).append("\n");
            orderDetails.append("   Số lượng: ").append(order.getQuantity()).append("\n");
            orderDetails.append("   Đơn giá: ").append(order.getPricePerUnit()).append("\n");
            orderDetails.append("   Tổng tiền: ").append(order.getPricePerUnit()*order.getQuantity()).append("\n");
            orderDetails.append("-------------------------------------\n");
            if (flag == 1) {
                body = "Kính gửi quý khách hàng,\n\n" +
                        "Chúng tôi xin thông báo về đơn hàng của quý khách như sau:\n" +
                        orderDetails.toString() +
                        "Trạng thái: Đã tiếp nhận đơn hàng. Chúng tôi sẽ nhanh chóng giao hàng cho quý khách.\n" +
                        "Cảm ơn quý khách đã sử dụng dịch vụ của chúng tôi.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Hỗ trợ khách hàng";
            } else {
                body = "Kính gửi quý khách hàng,\n\n" +
                        "Chúng tôi thành thật xin lỗi vì đã gặp sự cố khi xử lý đơn hàng của quý khách.\n" +
                        orderDetails.toString() +
                        "Trạng thái: Xử lý thất bại\n" +
                        "Lý do: Không đủ số lượng hàng trong kho\n" +
                        "Chúng tôi thành thật xin lỗi vì sự bất tiện này. Xin vui lòng liên hệ với chúng tôi để được hỗ trợ thêm.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ Hỗ trợ khách hàng";
            }

            EmailSender.sendEmail(recipientEmail,subject,body);
            System.out.println(response);
        }
    }

    private boolean checkProductAvailability(Long productId, Integer quantity) {
        int availableQuantity = productService.getProductQuantity(productId);
        return availableQuantity >= quantity;
    }

}