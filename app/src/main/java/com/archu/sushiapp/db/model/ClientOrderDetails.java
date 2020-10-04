package com.archu.sushiapp.db.model;


import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientOrderDetails {

    private UUID uuid;
    private String timeOfDelivery, email, firstName, lastName,
            phoneNumber, street, houseNumber, apartmentNumber, orderDetails;
    private Date ts;
    private Boolean paid;
    private Double totalPrice;

}
