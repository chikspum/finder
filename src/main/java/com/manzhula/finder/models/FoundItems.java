package com.manzhula.finder.models;

import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FoundItems {
    Long chatId;
    String query;
    Integer messageToDel;
    Double avgPrice;
    List<ItemDto> foundItems;
    Boolean isSubscribed;
}
