package com.manzhula.finder.config;

import com.manzhula.finder.models.FoundItems;
import com.manzhula.finder.models.ItemDto;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCache {
    List<ItemDto> itemList;
    Double minPrice;
    Double maxPrice;
    Double avgPrice;
    String place;
    String notEquals;
    boolean onFilter;
    String filterToken;
}
