package com.manzhula.finder.models;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDto {
    @EqualsAndHashCode.Exclude
    private String itemId;
    private String itemName;
    private String itemURL;
    private String itemPhotoURL;
    private double itemPrice;
    private String itemSource;
    private Boolean itemPresence;
}
