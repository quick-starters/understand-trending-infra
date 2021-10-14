# Spring Kafka

Spring Kafka 실습을 다룹니다.



### 연관 기사

#### baeldung blog

- [Intro to Apache Kafka with Spring](https://www.baeldung.com/spring-kafka)
- [Testing Kafka and Spring Boot](https://www.baeldung.com/spring-boot-kafka-testing)
- [Monitor the Consumer Lag in Apache Kafka](https://www.baeldung.com/java-kafka-consumer-lag)
- [Send Large Messages With Kafka](https://www.baeldung.com/java-kafka-send-large-message)
- [Configuring Kafka SSL Using Spring Boot](https://www.baeldung.com/spring-boot-kafka-ssl)

#### spring.io

- [Spring Kafka Guide](https://spring.io/projects/spring-kafka)



### Intro

This is a simple Spring Boot app to demonstrate sending and receiving of messages in Kafka using spring-kafka.

As Kafka topics are not created automatically by default, this application requires that you create the following topics manually.

이 앱은 `spring-kafka` 모듈을 사용하여 카프카에서 메시지를 보내고 받는 것을 시연하는 간단한 스프링 부트 앱입니다.

기본적으로 Kafka `topic`은 자동으로 작성되지 않으므로, 다음 생성해야합니다.

```bash
// baeldung 토픽 생성
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 -- replication-factor 1 --partitions 1 --topic baeldung

// partitioned 토픽 생성 (파티션 수 5개)
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 5 --topic partitioned

// filtered 토픽 생성
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic filtered

// greeting 토픽 생성
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic greeting
```



앱이 성공적으로 실행되면 콘솔에서 다음 로그를 확인할 수 있습니다.

#### Message received from the 'baeldung' topic by the basic listeners with groups foo and bar
>Received Message in group 'foo': Hello, World!<br>
Received Message in group 'bar': Hello, World!

#### Message received from the 'baeldung' topic, with the partition info
>Received Message: Hello, World! from partition: 0

#### Message received from the 'partitioned' topic, only from specific partitions
>Received Message: Hello To Partioned Topic! from partition: 0<br>
Received Message: Hello To Partioned Topic! from partition: 3

#### Message received from the 'filtered' topic after filtering
>Received Message in filtered listener: Hello Baeldung!

#### Message (Serialized Java Object) received from the 'greeting' topic
>Received greeting message: Greetings, World!!

