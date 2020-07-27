package com.archu.sushiapp.model;

import com.google.android.gms.maps.model.LatLng;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant {

    private String street;
    private LatLng latLng;

}
