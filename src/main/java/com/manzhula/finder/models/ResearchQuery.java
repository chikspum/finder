package com.manzhula.finder.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;


@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResearchQuery {
    Long chatId;
    String query;
    String localDateTime;
    Integer messageToDel;
}
