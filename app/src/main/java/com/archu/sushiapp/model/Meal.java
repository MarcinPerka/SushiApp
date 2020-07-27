package com.archu.sushiapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meal {

    private Integer id;
    private String name, description, imageLink, category;
    private Double price;

    @Override
    public String toString() {
        return name + " cena: " + price + " zł";
    }
}
