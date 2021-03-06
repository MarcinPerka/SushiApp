package com.archu.sushiapp.db.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPrice {

    private double price, minDistance, maxDistance;

}
