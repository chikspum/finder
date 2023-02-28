package com.manzhula.finder.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Subscribe {
    private Integer id;
    private String itemId;
    private String userId;
}