package com.zaalima.orderservice.config;

import com.zaalima.orderservice.avro.PaymentSuccessEvent;
import com.zaalima.orderservice.kafka.AvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaymentSuccessAvroKafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PaymentSuccessEvent> paymentSuccessAvroConsumerFactory() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-payment-avro-group"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new AvroDeserializer<>(PaymentSuccessEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent>
    paymentSuccessAvroKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentSuccessAvroConsumerFactory());

        return factory;
    }
}
