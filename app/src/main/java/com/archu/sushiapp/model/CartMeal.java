package com.archu.sushiapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartMeal {

    private Meal meal;
    private Integer quantity;

    @Override
    public String toString() {
        return meal.toString() + " " +
                "ilość: " + quantity + "\n";
    }
}
