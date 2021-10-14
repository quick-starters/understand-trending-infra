package com.quickstarters.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    @Value(value = "${spring.kafka.template.default-topic}")
    private String topicName;

    private final MessageProducer producer;

    @Autowired
    KafkaController(MessageProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public String sendMessage(@RequestParam("message") String message) {
        this.producer.sendMessage(topicName, message);

        return "success";
    }
}
