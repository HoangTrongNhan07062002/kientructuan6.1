package vn.edu.fit.util;

import org.springframework.messaging.MessagingException;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {
    public static void sendEmail(String recipientEmail, String subject, String body) throws MessagingException {

        //Get properties object
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        final String username = "tuthanh375@gmail.com";


        final String password = "YOUR_PASSWORD";


        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // compose message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);

            System.out.println("Gửi email thành công!");
        } catch (MessagingException | AddressException e) {
            throw new MessagingException("Lỗi khi gửi email: " + e.getMessage());
        } catch (javax.mail.MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
