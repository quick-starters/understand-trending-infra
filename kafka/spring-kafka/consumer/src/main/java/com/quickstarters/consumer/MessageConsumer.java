package com.quickstarters.consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String message) {
        System.out.println("Received Message in group quick-starters-consumer-group: " + message);
    }

    @KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            groupId = "filter",
            containerFactory = "filterKafkaListenerContainerFactory"
    )
    public void listenWithFilter(String message) {
        System.out.println("Received Message in filtered listener: : " + message);
    }

}
