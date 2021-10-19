package com.quickstarters.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class MessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    private ProducerRecord<String, String> createRecord(String topicName, String data) {
        return new ProducerRecord<>(topicName, data);
    }

    // 동기 전송
    public void sendMessage(String topicName, String message) {

        try {
            SendResult<String, String> sendResult = kafkaTemplate.send(createRecord(topicName, message)).get();
            System.out.println("Sent message=[" + message + "] with offset=[" + sendResult.getRecordMetadata().offset() + "]");
            // handleSuccess(topicName, message);
        }
        catch (ExecutionException e) {
            // handleFailure(topicName, message, e.getCause());
        }
        catch (InterruptedException e) {
            // handleFailure(topicName, message, e);
        }
    }

    // 비동기 전송
    public void sendMessageAsync(String topicName, String message) {

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> sendResult) {
                System.out.println("Sent message=[" + message + "] with offset=[" + sendResult.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }
}