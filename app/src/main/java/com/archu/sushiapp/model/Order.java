package com.archu.sushiapp.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private List<CartMeal> cartMeals;
    private double price;
    private DiscountCode discountCode;
    private double deliveryCost;

    @Override
    public String toString() {
        double priceWithDiscount = Math.round((price - (price * discountCode.getDiscount())) * 100.0) / 100.0;
        String discountCodeStr = "brak\n";
        if (discountCode != null) {
            discountCodeStr = discountCode.getCode() + ": " + discountCode.getDiscount() * 100 + " %\n";
        }
        return printList(cartMeals) +
                "Koszt zamówienia: " + price + " zł\n" +
                "Kod rabatowy: " + discountCodeStr +
                "Koszt zamówienia uwzględniając rabat: " + priceWithDiscount + " zł\n" +
                "Koszt dowozu: " + deliveryCost + " zł";
    }

    private String printList(List<CartMeal> cartMeals) {
        StringBuilder stringBuilder = new StringBuilder();
        for (CartMeal element : cartMeals)
            stringBuilder.append(element.toString() + "\n");
        return stringBuilder.toString();
    }
}
