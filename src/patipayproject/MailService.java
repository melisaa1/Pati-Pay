
package patipayproject;

import jakarta.mail.*;
import jakarta.mail.internet.*; 
import java.util.Properties;

public class MailService {

    public static void sendMail(String to, String subject, String body) {
        final String fromEmail = "melisa08486847@gmail.com"; 
        final String password = "mexzywohszcyctae";     

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("✅ E-posta gönderildi!");
        } catch (MessagingException e) {
            System.out.println("❌ E-posta gönderilemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
