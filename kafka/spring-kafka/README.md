# Spring Kafka
`Spring For Apache Kafka` 모듈을 이용해 Kafka 기반 메시징 솔루션을 개발 실습을 진행합니다.



## Kafka 설치 및 실행

### kafka container 

실습을 위해 카프카를 띄워야합니다. Docker를 이용해 Kafka 컨테이너를 띄워보도록 하겠습니다.



**docker-compose-single-broker.yml**

```yml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka:2.13-2.7.1
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_CREATE_TOPICS: "test:1:1" # Topic명:Partition개수:Replica개수
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock

```

docker compose 를 통해 하나의 broker(kafka)와 zookeeper를 띄웁니다. docker compose 파일은 위와 같습니다.

- KAFKA_ADVERTISED_HOST_NAME : 카프카가 띄워질 호스트 IP

- KAFKA_CREATE_TOPICS : 기본 생성 토픽 정보

- KAFKA_ZOOKEEPER_CONNECT : 연결될 zookeeper 정보



다음의 명령어로 kafka와 zookeeper를 띄웁니다.

```bash
docker compose -f docker-compose-single-broker.yml up -d
```



`docker ps` 로 띄워진 이미지를 확인합니다.

![image-20211010142835906](/Users/addpage/Library/Application Support/typora-user-images/image-20211010142835906.png)



### kafka client

띄워진 kafka에 명령어를 콘솔 상에서 명령어를 내리기 위해 client를 다운 받습니다.

````bash
wget http://apache.mirror.cdnetworks.com/kafka/2.7.1/kafka_2.13-2.7.1.tgz
````



`cd kafka_2.13-2.7.1.tgz/bin` 으로 이동하게되면 client가 제공하는 쉘 기반 명령어가 존재합니다.

![image-20211014181143492](/Users/addpage/Library/Application Support/typora-user-images/image-20211014181143492.png)



### kafka cli client 

다음의 명령어들을 실행해봅시다.



**토픽 목록 보기**

명령어

```bash
kafka-topics.sh \
	--zookeeper zookeeper:2181 \
	--list
```

결과

```bash
__consumer_offsets
test
```



**토픽 상세 보기**

명령어 

```bash
kafka-topics.sh \
	--zookeeper zookeeper:2181 \
	--topic test \
  --describels
```

결과

```
Topic: test	PartitionCount: 1	ReplicationFactor: 1	Configs:
	Topic: test	Partition: 0	Leader: none	Replicas: 1001	Isr: 1001
```

첫 줄은 토픽에 대한 기본 정보, 아랫줄부터는 각 파티션에 대한 정보가 나옵니다. 파티션 개수는 1개, 리플리케이션 팩터는 1개로 나타납니다.



**콘솔 프로듀서 열기**

```bash
./kafka-console-producer.sh \
	--broker-list localhost:9092 \
	--topic test
```



**콘솔 컨슈머 열기**

주기적으로 poll을 통해 kafka 메시지를 consume 합니다.

```bash
./kafka-console-consumer.sh \
	--bootstrap-server localhost:9092 \
	--topic test \
	--from-beginning
```



## Spring Kafka 실습

> ✏️ 예시 프로젝트는 Spring multi module로 구성되어있습니다.
>
> ```
> spring-kafka
>   - producer
>   - consumer
> ```



### 종속성 설정

종속성을 다음과 같이 설정합니다.

**build.gradle**

```groovy
plugins {
    id 'org.springframework.boot' version '2.5.5'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'java'
}

group = 'com.quick-starters'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.kafka:spring-kafka:2.7.1'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.kafka:spring-kafka-test'
}

test {
    useJUnitPlatform()
}

```

> ✏️ 위의 설정은 spring initializer에서 `Spring for Apache Kafka`를 선택하면 더 쉽게 설정이 가능합니다.



### Topic 생성

> 기본적으로 Producer가 존재하지 않는 Topic 명으로 데이터를 전송하면, 자동으로 생성되도록 옵션이 설정되어 있습니다. 

위에서 CLI로 생성하는 방법을 다뤘었습니다. 하지만 이제 `AdminClient`가 도입되어 프로그래밍 방식으로  topic을 생성할 수 있습니다. 



다음과 같이 `Configuration`을 생성하면, 자동으로 topic을 등록해줍니다.

**KafkaTopicConfig.java**

```java
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
```

> ⚠️ Spring Boot를 사용할 때 `KafkaAdmin` 빈은 자동으로 등록되므로 `NewTopic` 및  `NewTopics`만 있어도 됩니다.



**결과**

```bash
❯ ./kafka-topics.sh --zookeeper localhost:2181 --list
__consumer_offsets
defaultBoth
defaultPart
defaultRepl
quick-starters-log
test # docker container 뜰 때 자동으로 생김 (위의 docker-compose 파일 참고)
```



### Producer 구현

`Spring For Apache Kafka` 모듈은 `KafkaTemplate`이라는 것을 제공합니다. producer를 감싸고 topic에 데이터를 전송하도록 감싼 인터페이스입니다.



#### KafkaTemplate 로 메시지 전송

다음과 같은 메소드를 제공합니다. 자세한 정보는 [Javadoc](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/KafkaTemplate.html) 참고.

```java
ListenableFuture<SendResult<K, V>> sendDefault(V data);
ListenableFuture<SendResult<K, V>> sendDefault(K key, V data);
ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, K key, V data);
ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, Long timestamp, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, V data);
ListenableFuture<SendResult<K, V>> send(String topic, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, Long timestamp, K key, V data);
ListenableFuture<SendResult<K, V>> send(ProducerRecord<K, V> record);
ListenableFuture<SendResult<K, V>> send(Message<?> message);

Map<MetricName, ? extends Metric> metrics();

List<PartitionInfo> partitionsFor(String topic);

<T> T execute(ProducerCallback<K, V, T> callback);

// Flush the producer.
void flush();

interface ProducerCallback<K, V, T> {
    T doInKafka(Producer<K, V> producer);
}
```



KafkaTemplate을 사용하기 위해 ProducerFactory를 구성하고 KafkaTemplate를 생성하는 설정 파일을 등록합니다.

**KafkaProducerConfig.java**

```java
@Configuration
public class KafkaProducerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // See https://kafka.apache.org/documentation/#producerconfigs for more properties
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

```

> producer 설정은 application.properties, application.yml 을 통해서도 가능합니다.
>
> ```yaml
> server.port: 2000
> 
> spring:
>   kafka:
>     bootstrap-servers: localhost:9092
> 
>     producer:
> #      key-serializer: org.apache.kafka.common.serialization.StringSerializer
> #      value-serializer: org.apache.kafka.common.serialization.StringSerializer
> 
>     template:
>       default-topic: quick-starters-log
> ```



**동기 전송**

프로듀서는 메시지를 보내고 send() 메소드의 Future 객체를 리턴합니다. get() 메소드를 사용해 Future를 기다린 후 send()가 성공했는지 실패했는지를 확인합니다. 이러한 방법을 통해 메시지마다 브로커에게 전송한 메시지가 성공했는지 실패했는지 확인하여 더욱 신뢰성 있는 메시지 전송을 할 수 있습니다.

```java

public void sendMessage(String message) {

  try {
    kafkaTemplate.send(createRecord(topicName, message)).get();
    // handleSuccess(topicName, message);
  }
  catch (ExecutionException e) {
    // handleFailure(topicName, message, e.getCause());
  }
  catch (InterruptedException e) {
    // handleFailure(topicName, message, e);
  }
}
```

get() 메소드를 이용해 카프카의 응답을 기다립니다. 성공적으로 전송되지 않으면 에러를, 에러가 없다면 메시지가 기록된 오프셋을 알 수 있는 RecordMetadata를 얻게 됩니다.



**비동기 전송**

프로듀서는 send() 메소드를 콜백과 같이 호출하고 카프카 브로커에서 응답을 받으면 콜백합니다. 만약 보낸 모든 메시지에 대해 응답을 기다린다면 응답을 기다리는 시간이 많이 소요됩니다. 하지만 비동기적으로 전송한다면 응답을 기다리지 않기 때문에 더욱 빠른 전송이 가능합니다.

```java
// 비동기 전송
public void sendMessageAsync(String topicName, String message) {

	ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);

	future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

    @Override
    public void onSuccess(SendResult<String, String> sendResult) {
      System.out.println("Sent message=[" + message + "] with offset=[" + 		sendResult.getRecordMetadata().offset() + "]");
    }

    @Override
    public void onFailure(Throwable ex) {
      System.out.println("Unable to send message=[" + message + "] due to : " + 	ex.getMessage());
    }
	});
}
```



producer 전체 코드는 다음과 같습니다.

**MessageProducer.java**

```java
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
```



### Cosumer 구현

`Spring For Apache Kafka` 모듈은 `@KafkaListener` 주석을 제공합니다. 이를 통해 메시지를 수신할 수 있습니다. 또한 `MessageListenerContainer` 를 설정하고 메시지 리스너에 제공할 수 있습니다.



#### KafkaListener 로 메시지 소비

다음과 같은 메소드를 제공합니다. 자세한 정보는 [Javadoc](https://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/KafkaTemplate.html) 참고.



**KafkaConsumerConfig.java**

다음과 같이 설정파일을 통해 기본 consumerFatory를 제공합니다.

```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

}
```



**MessageConsumer.java**

기본 컨슈머 팩토리를 통해 명시된 topic, groupId로부터 메시지를 소비합니다.

```java
@Component
public class MessageConsumer {

		@KafkaListener(
            topics = "${spring.kafka.template.default-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(String message) {
        System.out.println("Received Message in group quick-starters-consumer-group: " + message);
    }

}
```



### 실습

멀티 모듈로 구성하였기 때문에 다음과 같이 consumer, producer 두 앱을 띄울 수 있습니다.

![image-20211014204948262](/Users/addpage/Library/Application Support/typora-user-images/image-20211014204948262.png)



**produce 메시지**

`/kafka` 경로로 메시지 parameter와 함께 전송하면 메시지를 produce 하도록 구현했습니다. (실습 코드 참고)

![image-20211014204240204](/Users/addpage/Library/Application Support/typora-user-images/image-20211014204240204.png)



**consume 결과**

![image-20211014204225446](/Users/addpage/Library/Application Support/typora-user-images/image-20211014204225446.png)



## 해볼만한 실습

- Producer Ack 설정에 따른 장애 
- 수동 커밋, 자동 커밋 
- 파티션 개수, 컨슈머 그룹 개수에 따른 컨슘 순서
- 데이터 파이프라인 구축



## 연관 기사

### baeldung blog

- [Intro to Apache Kafka with Spring](https://www.baeldung.com/spring-kafka)
- [Testing Kafka and Spring Boot](https://www.baeldung.com/spring-boot-kafka-testing)
- [Monitor the Consumer Lag in Apache Kafka](https://www.baeldung.com/java-kafka-consumer-lag)
- [Send Large Messages With Kafka](https://www.baeldung.com/java-kafka-send-large-message)
- [Configuring Kafka SSL Using Spring Boot](https://www.baeldung.com/spring-boot-kafka-ssl)

### spring.io

- [Spring Kafka Guide](https://docs.spring.io/spring-kafka/docs/current/reference/html)

