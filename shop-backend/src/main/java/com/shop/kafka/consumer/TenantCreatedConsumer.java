package com.shop.kafka.consumer;

import com.shop.auth.repository.UserRepository;
import com.shop.kafka.dto.TenantCreatedEvent;
import com.shop.kafka.entity.PendingCrmTenant;
import com.shop.kafka.repository.PendingCrmTenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedConsumer {

    private final UserRepository userRepository;
    private final PendingCrmTenantRepository pendingRepo;

    @KafkaListener(
            topics = "${app.kafka.topics.crm-tenant-created:crm.tenant.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "tenantCreatedListenerFactory"
    )
    @Transactional
    public void onTenantCreated(ConsumerRecord<String, TenantCreatedEvent> record, Acknowledgment ack) {
        TenantCreatedEvent event = record.value();
        log.info("Kafka received TenantCreated: tenantId={} schema={} adminEmail={}",
                event.getTenantId(), event.getTenantSchema(), event.getAdminEmail());

        try {
            if (event.getAdminEmail() == null || event.getTenantSchema() == null) {
                log.warn("TenantCreatedEvent missing required fields, skipping");
                ack.acknowledge();
                return;
            }

            var userOpt = userRepository.findByEmail(event.getAdminEmail());
            if (userOpt.isPresent()) {
                // Юзер уже есть — сразу обновляем схему
                userRepository.updateCrmTenantSchema(event.getAdminEmail(), event.getTenantSchema());
                log.info("Updated crmTenantSchema for existing user {}: {}", event.getAdminEmail(), event.getTenantSchema());
            } else {
                // Юзер ещё не зарегистрировался — сохраняем в pending
                // Если уже есть pending для этого email — обновляем схему (re-registration case)
                pendingRepo.findByEmail(event.getAdminEmail()).ifPresentOrElse(
                        existing -> {
                            existing.setTenantSchema(event.getTenantSchema());
                            pendingRepo.save(existing);
                            log.info("Updated existing pending tenant for email={}", event.getAdminEmail());
                        },
                        () -> {
                            pendingRepo.save(PendingCrmTenant.builder()
                                    .id(UUID.randomUUID())
                                    .email(event.getAdminEmail())
                                    .tenantSchema(event.getTenantSchema())
                                    .receivedAt(Instant.now())
                                    .build());
                            log.info("Saved pending CRM tenant for email={} — user not registered yet", event.getAdminEmail());
                        }
                );
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process TenantCreatedEvent: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
