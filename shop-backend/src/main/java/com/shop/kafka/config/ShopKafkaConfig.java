package com.shop.kafka.config;

import com.shop.common.config.AppProperties;
import com.shop.kafka.dto.OrderStatusChangedEvent;
import com.shop.kafka.dto.ProductSyncEvent;
import com.shop.kafka.dto.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.*;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;
import java.util.Map;

@Configuration @RequiredArgsConstructor
public class ShopKafkaConfig {
    private final KafkaProperties springKafkaProps;
    private final AppProperties appProps;

    // ── Topics ──
    @Bean public NewTopic topicShopOrderCreated() {
        return TopicBuilder.name(appProps.getKafka().getTopics().getShopOrderCreated()).partitions(3).replicas(1).build();
    }
    @Bean public NewTopic topicCrmStatusChanged() {
        return TopicBuilder.name(appProps.getKafka().getTopics().getCrmOrderStatusChanged()).partitions(3).replicas(1).build();
    }
    @Bean public NewTopic topicProductsSync() {
        return TopicBuilder.name(appProps.getKafka().getTopics().getCrmProductsSync()).partitions(3).replicas(1).build();
    }

    // ── Producer (JSON) ──
    @Bean public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>(springKafkaProps.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props);
    }
    @Bean public KafkaTemplate<String, Object> kafkaTemplate() { return new KafkaTemplate<>(producerFactory()); }

    // ── String Producer (for Outbox) ──
    @Bean public KafkaTemplate<String, String> stringKafkaTemplate() {
        Map<String, Object> props = new HashMap<>(springKafkaProps.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    // ── Consumer for OrderStatusChanged ──
    @Bean public ConsumerFactory<String, OrderStatusChangedEvent> statusConsumerFactory() {
        Map<String, Object> props = new HashMap<>(springKafkaProps.buildConsumerProperties(null));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        var deser = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(OrderStatusChangedEvent.class, false));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
    }

    @Bean public ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent>
    statusListenerFactory(ConsumerFactory<String, OrderStatusChangedEvent> cf) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, OrderStatusChangedEvent>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));
        return factory;
    }

    // ── Consumer for ProductSync ──
    @Bean public ConsumerFactory<String, ProductSyncEvent> productSyncConsumerFactory() {
        Map<String, Object> props = new HashMap<>(springKafkaProps.buildConsumerProperties(null));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        var deser = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ProductSyncEvent.class, false));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
    }

    @Bean public ConcurrentKafkaListenerContainerFactory<String, ProductSyncEvent>
    productSyncListenerFactory(ConsumerFactory<String, ProductSyncEvent> cf) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ProductSyncEvent>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));
        return factory;
    }

    // ── Consumer for TenantCreated ──
    @Bean public ConsumerFactory<String, TenantCreatedEvent> tenantCreatedConsumerFactory() {
        Map<String, Object> props = new HashMap<>(springKafkaProps.buildConsumerProperties(null));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        var deser = new ErrorHandlingDeserializer<>(new JsonDeserializer<>(TenantCreatedEvent.class, false));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deser);
    }

    @Bean public ConcurrentKafkaListenerContainerFactory<String, TenantCreatedEvent>
    tenantCreatedListenerFactory(ConsumerFactory<String, TenantCreatedEvent> cf) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, TenantCreatedEvent>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));
        return factory;
    }
}
