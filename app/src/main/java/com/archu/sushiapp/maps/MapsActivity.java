package com.archu.sushiapp.maps;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        mMap.setMaxZoomPreference(17.0f);
        MainActivity.restaurants.forEach(restaurant ->
                mMap.addMarker(new MarkerOptions().position(restaurant.getLatLng()).title(restaurant.getStreet()))
        );

        LatLng userLocationLatLng;
        try {
            userLocationLatLng = new LatLng(MainActivity.userLocation.getResult().getLatitude(), MainActivity.userLocation.getResult().getLongitude());
        } catch (Exception e) {
            userLocationLatLng = new LatLng(51.776652, 19.489312);
        }

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(userLocationLatLng).title("Twoja lokalizacja"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocationLatLng));
    }
}
