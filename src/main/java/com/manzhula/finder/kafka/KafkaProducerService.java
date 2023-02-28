package com.manzhula.finder.kafka;

import com.manzhula.finder.botcontroller.FinderController;
import com.manzhula.finder.models.ResearchQuery;
import com.manzhula.finder.models.Subscribe;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);

    KafkaTemplate<String, ResearchQuery> kafkaTemplate;

    KafkaTemplate<String, Subscribe> kafkaTemplate1;

    KafkaProducerService(KafkaTemplate<String, ResearchQuery> kafkaTemplate, KafkaTemplate<String, Subscribe> kafkaTemplate1) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate1 = kafkaTemplate1;
    }

    public int sendResearchRequest(String topic, ResearchQuery query) {
        kafkaTemplate.send(topic, query);
        LOGGER.info("Send message to '" + topic + "': " + query);
        return 0;
    }

    public int sendSubscribeRequest(String topic, Subscribe query) {
        kafkaTemplate1.send(topic, query);
        LOGGER.info("Send message to '" + topic + "': " + query);
        return 0;
    }

//    @KafkaListener(topics = "${KAFKA_TOPIC_PREFIX}research-request", groupId = "${KAFKA_USERNAME}-consumer")
//    public void listenGroupFoo(@Payload ResearchQuery message) {
//        LOGGER.info("Received Message: " + message);
//    }
}
