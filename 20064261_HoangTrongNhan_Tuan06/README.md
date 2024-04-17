# Hệ thống đặt hàng từ xa với messaging service (ActiveMQ)

## Giới thiệu
> Đây là hệ thống đặt hàng từ xa, cho phép khách hàng đặt hàng thông qua web. Thông tin đặt hàng được mã hóa và gửi đến hệ thống thông qua messaging service. Hệ thống sau đó xác nhận hoặc hủy bỏ đơn hàng và gửi email thông báo tình trạng đơn hàng cho khách hàng.

**Lưu ý**: Đây chỉ là demo hệ thống đơn giản, do đó mỗi lần mua chỉ chọn được 1 sản phẩm (không có giỏ hàng), và customerID luôn mặc định là 1.
## Quy Trình Đặt Hàng

1. **Khách hàng chọn sản phẩm:** Khách hàng chọn sản phẩm, số lượng và gửi thông tin đặt hàng.

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/80d280d7-8004-47c7-b0d7-f40227113e4d)

2. **Mã hóa thông tin:** Thông tin đặt hàng bao gồm `id`, `name`, `quantity`, và `pricePerUnit` được chuyển đổi thành định dạng JSON và được mã hóa bằng Base64.

```js
<script>
const sendOrderData = (button) => {
        const row = button.closest('tr');
        const productId = row.querySelector('[name="id"]').value;
        const productName = row.querySelector('[name="name"]').textContent;
        const quantity = row.querySelector('[name="quantity"]').value;
        const price = row.querySelector('[name="price"]').textContent;

        var data = {
            id: productId,
            name: productName,
            quantity: quantity,
            pricePerUnit: price
        };

        //convert to JSON
        var jsonData = JSON.stringify(data);

        //encrypt by Base64
        var base64EncodedData = CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(jsonData));

        document.getElementById('encryptedData').value = base64EncodedData;
        document.getElementById('orderForm').submit();
    }
<script>
```
   
3. **Gửi thông tin đến API:** Thông tin mã hóa được POST lên API tại endpoint `/orders`.

```js
  <form id="orderForm" action="/orders" method="post">
```
   
4. **Xử lý thông tin:** Dữ liệu nhận được từ endpoint `/orders` sẽ được gửi đến destination `order_products` thông qua `jmsTemplate`.

```js
    @PostMapping("/orders")
    public String purchaseOrder(@RequestParam String encryptedData) throws Exception {

        // Gửi dữ liệu đã mã hóa đến queue "order_products"
        jmsTemplate.convertAndSend("order_products", encryptedData);
        ResponseEntity.ok().body("Order received. Processing...");

        return "redirect:/products";
    }
```
   
5. **Nhận và xử lý thông tin:** `OrderMessageListener` với Annotation `@JmsListener(destination = "order_products")` nhận dữ liệu đơn hàng từ `jmsTemplate`.
Dữ liệu được giải mã từ Base64 để lấy thông tin đặt hàng. Hệ thống kiểm tra số lượng sản phẩm trong kho dựa trên thông tin đặt hàng. Nếu số lượng trong kho đủ, hệ thống sẽ tạo đơn hàng. Nếu không, đơn hàng sẽ bị từ chối.
Dù đơn hàng được xác nhận hay từ chối, hệ thống sẽ gửi email (thông qua `SMTP`) đến khách hàng để thông báo tình trạng đơn hàng.
```js
@JmsListener(destination = "order_products")
    public void receiveMessage(final Message jsonMessage) throws JMSException {
        String messageData = null;
        String response = null;
        System.out.println("...Đơn hàng đã nhận..");
        int flag = 0;

        if (jsonMessage instanceof TextMessage textMessage) {
            messageData = textMessage.getText();

            // Giải mã dữ liệu message (Base64)
            String decodedString = Decryptor.decryptBase64ToJson(messageData);
            Gson gson = new Gson();
            OrderDTO order = gson.fromJson(decodedString, OrderDTO.class);

            // Kiểm tra số lượng trong kho
            boolean purchaseResult = checkProductAvailability(order.getProductId(), order.getQuantity());
            Orders orders = new Orders();

            // Quyết định đơn hàng có được xác nhận hay không
            if (purchaseResult) {
                orders = orderService.purchaseProduct(order.getProductId(), order.getName(), order.getQuantity(), order.getPricePerUnit());
                response = "Đặt hàng thành công";
                flag = 1;
            } else {
                response = "Đơn hàng bị từ chối vì không đủ số lượng trong kho";
                flag = 0;
            }
             // Gửi email tới khách hàng
            String recipientEmail = "customer@example.com"; //email of customer
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
```


## Kết quả chi tiết 
### 1. Trường hợp trong kho còn đủ số lượng, đơn hàng được xác nhận

Trong cửa sổ console log ra thông tin gồm: 
- Thông báo nhận được đơn hàng
- Dữ liệu đơn hàng được mã hóa
- Dữ liệu đơn hàng được giải mã
- Gửi email thành công
- Xác nhận đơn hàng

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/f56acf1f-43bf-454b-a92a-d070fdc6c4aa)

Trong ActiveMQ (http://127.0.0.1:8161/admin/queues.jsp) có queue `order_products` với số lượng tin nhắn đã được gửi đến và thêm vào hàng đợi để chờ xử lý là 1

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/823e8874-915b-4e5c-8284-1f2e86932a5a)

Khách hàng nhận được email thông báo về đơn hàng

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/8d4482b1-19c6-4344-8a10-9d8f15a05d07)

Đơn hàng được lưu vào cơ sở dữ liệu 

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/b1aca558-5442-4808-8e03-867253da4f6d)

### 2. Trường hợp trong kho không đủ số lượng, đơn hàng bị từ chối

Trong cửa sổ console log ra thông tin gồm: 
- Thông báo nhận được đơn hàng
- Dữ liệu đơn hàng được mã hóa
- Dữ liệu đơn hàng được giải mã
- Gửi email thành công
- Từ chối đơn hàng

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/288ae9cb-1a10-48e6-8a94-aef2f8825d68)

Trong ActiveMQ (http://127.0.0.1:8161/admin/queues.jsp) có queue `order_products` với số lượng tin nhắn đã được gửi đến và thêm vào hàng đợi để chờ xử lý là 2 (Trước đó đã có 1 lần gửi thông tin đặt hàng đến queue)

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/eec7d0ef-cc65-45bd-8a22-afeaf526abe3)

Khách hàng nhận được email thông báo về đơn hàng

![image](https://github.com/HaThiPhuongLinh/Week06_Software-Architecture-and-Design/assets/109422010/56ad7c47-6037-4906-a44a-3a8939159feb)

Đơn hàng không được lưu vào cơ sở dữ liệu 



