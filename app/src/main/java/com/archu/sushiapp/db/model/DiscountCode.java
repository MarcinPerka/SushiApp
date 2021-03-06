package com.archu.sushiapp.db.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountCode {

    private String code;
    private int count;
    private double discount;
}
