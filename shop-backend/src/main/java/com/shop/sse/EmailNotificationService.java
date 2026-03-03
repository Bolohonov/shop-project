package com.shop.sse;

import com.shop.common.config.AppProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j @Service @RequiredArgsConstructor
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties props;

    @Async
    public void sendOrderStatusChanged(String toEmail, String userName, String orderNumber,
                                        String newStatus, String comment) {
        try {
            Context ctx = new Context();
            ctx.setVariable("userName", userName);
            ctx.setVariable("orderNumber", orderNumber);
            ctx.setVariable("newStatus", newStatus);
            ctx.setVariable("comment", comment);
            String html = templateEngine.process("order-status-changed", ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(props.getEmail().getFrom(), props.getEmail().getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Статус заказа " + orderNumber + " изменён");
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to {} about order {}", toEmail, orderNumber);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
