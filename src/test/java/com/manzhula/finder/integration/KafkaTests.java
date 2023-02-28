package com.manzhula.finder.integration;

import com.manzhula.finder.botcontroller.FinderController;
import com.manzhula.finder.config.KafkaConfig;
import com.manzhula.finder.models.FoundItems;
import com.manzhula.finder.models.ItemDto;
import com.manzhula.finder.testconfig.KafkaTestConfig;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Import(KafkaTestConfig.class)
@RunWith(MockitoJUnitRunner.class)
public class KafkaTests {

    @Autowired
    KafkaTemplate<String, FoundItems> kafkaProducer;

    @SpyBean
    FinderController finderController;

    @Value("${LIST_SIZE}")
    Integer listSize;

    @Test
    @SneakyThrows
    public void messageTransferTests() {
        for (int i = 0; i < 10; i++) {
            kafkaProducer.send("u6qr12q4-research-response",
                    FoundItems.builder()
                            .chatId(439032305L)
                            .messageToDel(3435533)
                            .query("Some query" + i)
                            .foundItems(List.of(ItemDto.builder()
                                    .itemId("242342" + i)
                                    .itemPhotoURL("https://some.url")
                                    .itemPresence(true)
                                    .itemSource("https://prom.ua")
                                    .itemPrice(543)
                                    .itemName("Some name")
                                    .itemURL("https://another.url")
                                    .build()))
                            .build());
        }

        Thread.sleep(30_000);
//        verify(finderController, times(listSize)).printItem(any(), any(), any(), any());
    }

}
