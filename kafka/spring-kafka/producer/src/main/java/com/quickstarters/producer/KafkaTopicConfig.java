package com.quickstarters.producer;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Topic 구성
 * - https://docs.spring.io/spring-kafka/docs/current/reference/html/#configuring-topics
 */
@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.template.default-topic}")
    private String defaultTopicName;

    @Bean
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    // 단일 토픽 생성
    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(defaultTopicName)
                .partitions(1)
                .replicas(1)
                .compact()
                .build();
    }

    // 여러 토픽 생성
    @Bean
    public KafkaAdmin.NewTopics topics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name("defaultBoth")
                        .build(),
                TopicBuilder.name("defaultPart")
                        .replicas(1)
                        .build(),
                TopicBuilder.name("defaultRepl")
                        .partitions(3)
                        .build());
    }
}