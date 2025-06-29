package uz.pdp.interlex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.pdp.interlex.entity.ContactMessage;
import uz.pdp.interlex.entity.Lawyer;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:info@interlex.uz}")
    private String fromEmail;

    @Value("${app.admin.email:admin@interlex.uz}")
    private String adminEmail;

    public void sendAutoResponseEmail(ContactMessage message) {
        try {
            String subject = getAutoResponseSubject(message.getLangCode());
            String content = buildAutoResponseContent(message);

            sendHtmlEmail(message.getEmail(), subject, content);
            log.info("Auto-response email sent to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Failed to send auto-response email to: {}", message.getEmail(), e);
        }
    }

    public void sendNewMessageNotificationToAdmin(ContactMessage message) {
        try {
            String subject = "Yangi xabar - INTERLEX";
            String content = buildAdminNotificationContent(message);

            sendHtmlEmail(adminEmail, subject, content);
            log.info("Admin notification email sent for message ID: {}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send admin notification email for message ID: {}", message.getId(), e);
        }
    }

    public void sendLawyerAssignmentNotification(ContactMessage message, Lawyer lawyer) {
        try {
            if (!StringUtils.hasText(lawyer.getEmail())) {
                log.warn("Lawyer {} has no email address", lawyer.getId());
                return;
            }

            String subject = "Sizga yangi ish tayinlandi - INTERLEX";
            String content = buildLawyerAssignmentContent(message, lawyer);

            sendHtmlEmail(lawyer.getEmail(), subject, content);
            log.info("Assignment notification sent to lawyer: {}", lawyer.getEmail());
        } catch (Exception e) {
            log.error("Failed to send assignment notification to lawyer: {}", lawyer.getEmail(), e);
        }
    }

    public void sendStatusUpdateNotification(ContactMessage message) {
        try {
            String subject = getStatusUpdateSubject(message.getStatus(), message.getLangCode());
            String content = buildStatusUpdateContent(message);

            sendHtmlEmail(message.getEmail(), subject, content);
            log.info("Status update email sent to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Failed to send status update email to: {}", message.getEmail(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    private String getAutoResponseSubject(String langCode) {
        return switch (langCode != null ? langCode.toLowerCase() : "uz") {
            case "ru" -> "Спасибо за обращение - INTERLEX";
            case "en" -> "Thank you for contacting us - INTERLEX";
            default -> "Murojaatingiz uchun rahmat - INTERLEX";
        };
    }

    private String getStatusUpdateSubject(ContactMessage.MessageStatus status, String langCode) {
        String statusText = switch (langCode != null ? langCode.toLowerCase() : "uz") {
            case "ru" -> switch (status) {
                case IN_PROGRESS -> "Ваше обращение рассматривается";
                case RESPONDED -> "Мы ответили на ваше обращение";
                case CLOSED -> "Ваше обращение закрыто";
                default -> "Обновление статуса обращения";
            };
            case "en" -> switch (status) {
                case IN_PROGRESS -> "Your inquiry is being processed";
                case RESPONDED -> "We have responded to your inquiry";
                case CLOSED -> "Your inquiry has been closed";
                default -> "Inquiry status update";
            };
            default -> switch (status) {
                case IN_PROGRESS -> "Murojaatingiz ko'rib chiqilmoqda";
                case RESPONDED -> "Murojaatingizga javob berildi";
                case CLOSED -> "Murojaatingiz yopildi";
                default -> "Murojaat holati yangilandi";
            };
        };
        return statusText + " - INTERLEX";
    }

    private String buildAutoResponseContent(ContactMessage message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2563eb; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                    .contact-info { background: #e2e8f0; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚖️ INTERLEX</h1>
                        <p>Professional Yuridik Xizmatlar</p>
                    </div>
                    <div class="content">
                        <h2>Hurmatli %s!</h2>
                        <p>Bizning yuridik xizmatlarimizga murojaat qilganingiz uchun rahmat!</p>
                        
                        <div class="info-box">
                            <h3>📋 Sizning murojaatingiz ma'lumotlari:</h3>
                            <p><strong>Mavzu:</strong> %s</p>
                            <p><strong>Sana:</strong> %s</p>
                            <p><strong>Xizmat turi:</strong> %s</p>
                            <p><strong>Mijoz turi:</strong> %s</p>
                        </div>
                        
                        <p>Sizning murojaatingiz qabul qilindi va tez orada bizning mutaxassislarimiz siz bilan bog'lanishadi.</p>
                        
                        <div class="contact-info">
                            <h4>📞 Tezkor aloqa:</h4>
                            <p><strong>Telefon:</strong> +998 90 008-1-008</p>
                            <p><strong>Mobil:</strong> +998 71 250-21-25</p>
                            <p><strong>Email:</strong> info@interlex.uz</p>
                            <p><strong>Manzil:</strong> Toshkent sh., Yakkasaroy tumani, Sh.Rustaveli ko'chasi, 116/2</p>
                        </div>
                        
                        <p><strong>Eslatma:</strong> Agar sizda qo'shimcha savollar bo'lsa, bizga bemalol murojaat qiling. Biz 24/7 xizmat ko'rsatamiz.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 INTERLEX.UZ - Barcha huquqlar himoyalangan</p>
                        <p>Bu avtomatik xabar. Iltimos, javob bermang.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                message.getName(),
                message.getSubject(),
                message.getCreatedAt().format(formatter),
                message.getServiceType() != null ? message.getServiceType().getDisplayName() : "Ko'rsatilmagan",
                message.getClientType() != null ? message.getClientType().getDisplayName() : "Ko'rsatilmagan"
        );
    }

    private String buildAdminNotificationContent(ContactMessage message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc2626; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #dc2626; }
                    .urgent { background: #fef2f2; border: 1px solid #fecaca; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 YANGI XABAR</h1>
                        <p>INTERLEX Admin Panel</p>
                    </div>
                    <div class="content">
                        <div class="urgent">
                            <h3>⚠️ Diqqat: Yangi mijoz murojaati!</h3>
                            <p>Yangi xabar qabul qilindi va javob kutilmoqda.</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>👤 Mijoz ma'lumotlari:</h3>
                            <p><strong>Ism:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Telefon:</strong> %s</p>
                            <p><strong>Mijoz turi:</strong> %s</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>📋 Murojaat ma'lumotlari:</h3>
                            <p><strong>Mavzu:</strong> %s</p>
                            <p><strong>Xizmat turi:</strong> %s</p>
                            <p><strong>Sana:</strong> %s</p>
                            <p><strong>IP manzil:</strong> %s</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>💬 Xabar matni:</h3>
                            <p style="background: #f1f5f9; padding: 15px; border-radius: 5px; font-style: italic;">%s</p>
                        </div>
                        
                        <p><strong>Harakat talab etiladi:</strong> Iltimos, tez orada mijoz bilan bog'laning va javob bering.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                message.getName(),
                message.getEmail(),
                message.getPhone(),
                message.getClientType() != null ? message.getClientType().getDisplayName() : "Ko'rsatilmagan",
                message.getSubject(),
                message.getServiceType() != null ? message.getServiceType().getDisplayName() : "Ko'rsatilmagan",
                message.getCreatedAt().format(formatter),
                message.getClientIp() != null ? message.getClientIp() : "Noma'lum",
                message.getMessage()
        );
    }

    private String buildLawyerAssignmentContent(ContactMessage message, Lawyer lawyer) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #059669, #047857); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #059669; }
                    .priority { background: #fef3c7; border: 1px solid #f59e0b; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📋 YANGI ISH TAYINLANDI</h1>
                        <p>INTERLEX - Advokat Panel</p>
                    </div>
                    <div class="content">
                        <h2>Hurmatli %s %s!</h2>
                        
                        <div class="priority">
                            <h3>⚡ Diqqat: Sizga yangi ish tayinlandi!</h3>
                            <p>Mijoz bilan tez orada bog'lanish va javob berish talab etiladi.</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>👤 Mijoz ma'lumotlari:</h3>
                            <p><strong>Ism:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Telefon:</strong> %s</p>
                            <p><strong>Mijoz turi:</strong> %s</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>📋 Ish ma'lumotlari:</h3>
                            <p><strong>Mavzu:</strong> %s</p>
                            <p><strong>Xizmat turi:</strong> %s</p>
                            <p><strong>Qabul qilingan sana:</strong> %s</p>
                            <p><strong>Holat:</strong> Ko'rib chiqilmoqda</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>💬 Mijoz xabari:</h3>
                            <p style="background: #f1f5f9; padding: 15px; border-radius: 5px; font-style: italic;">%s</p>
                        </div>
                        
                        <p><strong>Keyingi qadamlar:</strong></p>
                        <ul>
                            <li>Mijoz bilan bog'laning</li>
                            <li>Masalani batafsil o'rganing</li>
                            <li>Kerakli hujjatlarni tayyorlang</li>
                            <li>Javobni admin panelda belgilang</li>
                        </ul>
                    </div>
                </div>
            </body>
            </html>
            """,
                lawyer.getFirstName(),
                lawyer.getLastName(),
                message.getName(),
                message.getEmail(),
                message.getPhone(),
                message.getClientType() != null ? message.getClientType().getDisplayName() : "Ko'rsatilmagan",
                message.getSubject(),
                message.getServiceType() != null ? message.getServiceType().getDisplayName() : "Ko'rsatilmagan",
                message.getCreatedAt().format(formatter),
                message.getMessage()
        );
    }

    private String buildStatusUpdateContent(ContactMessage message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String statusText = message.getStatus().getDisplayName();

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #2563eb, #1d4ed8); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; }
                    .info-box { background: white; padding: 20px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #2563eb; }
                    .status-update { background: #ecfdf5; border: 1px solid #10b981; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📊 HOLAT YANGILANDI</h1>
                        <p>INTERLEX - Murojaat holati</p>
                    </div>
                    <div class="content">
                        <h2>Hurmatli %s!</h2>
                        
                        <div class="status-update">
                            <h3>✅ Murojaatingiz holati yangilandi!</h3>
                            <p><strong>Yangi holat:</strong> %s</p>
                            <p><strong>Yangilanish vaqti:</strong> %s</p>
                        </div>
                        
                        <div class="info-box">
                            <h3>📋 Murojaat ma'lumotlari:</h3>
                            <p><strong>Mavzu:</strong> %s</p>
                            <p><strong>Qabul qilingan sana:</strong> %s</p>
                            <p><strong>Xizmat turi:</strong> %s</p>
                        </div>
                        
                        <p>Agar sizda qo'shimcha savollar bo'lsa, bizga bemalol murojaat qiling.</p>
                        
                        <div style="background: #e2e8f0; padding: 15px; border-radius: 8px; margin: 15px 0;">
                            <h4>📞 Aloqa ma'lumotlari:</h4>
                            <p><strong>Telefon:</strong> +998 90 008-1-008</p>
                            <p><strong>Email:</strong> info@interlex.uz</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                message.getName(),
                statusText,
                formatter.format(java.time.LocalDateTime.now()),
                message.getSubject(),
                message.getCreatedAt().format(formatter),
                message.getServiceType() != null ? message.getServiceType().getDisplayName() : "Ko'rsatilmagan"
        );
    }
}
