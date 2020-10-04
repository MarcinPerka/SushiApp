package com.archu.sushiapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.cart.CartFragment;
import com.archu.sushiapp.db.model.CartMeal;
import com.archu.sushiapp.db.model.Restaurant;
import com.archu.sushiapp.db.DbSession;
import com.archu.sushiapp.db.model.DiscountCode;
import com.archu.sushiapp.db.model.DeliveryPrice;
import com.archu.sushiapp.form.FormFragment;
import com.archu.sushiapp.db.model.Meal;
import com.archu.sushiapp.meal.MenuFragment;
import com.archu.sushiapp.maps.MapsActivity;
import com.archu.sushiapp.ocr.HandwritingRecognizeActivity;
import com.archu.sushiapp.ocr.RecognizeActivity;
import com.archu.sushiapp.db.model.Order;
import com.archu.sushiapp.order.OrderFragment;
import com.archu.sushiapp.db.model.SushiCategory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static List<Meal> meals;
    public static List<Restaurant> restaurants;
    public static List<CartMeal> cartMeals;
    public static List<SushiCategory> sushiCategories;
    public static List<DiscountCode> discountCodes;
    public static Order order = new Order();
    public static Task<Location> userLocation;
    public static List<DeliveryPrice> deliveryPrices;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        loadData();
        userLocation = fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                });

        if (getIntent().hasExtra("fragment"))
            runFragment();
        else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MainPanelFragment mainPanelFragment = new MainPanelFragment();
            mainPanelFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragmentContainer, mainPanelFragment, "Main Panel").addToBackStack(null).commit();
        }
    }

    private void runFragment() {
        if (getIntent().getIntExtra("fragment", 0) == 1) {
            replaceFragment(new CartFragment());
        } else if (getIntent().getIntExtra("fragment", 0) == 2) {
            replaceFragment(new MenuFragment());

        } else if (getIntent().getIntExtra("fragment", 0) == 3) {
            replaceFragment(new OrderFragment());
        } else {
            replaceFragment(new FormFragment());
        }
    }

    private void loadData() {
        if (meals == null && cartMeals == null && restaurants == null && sushiCategories == null && discountCodes == null) {
            DbSession dbSession = new DbSession();
            meals = new ArrayList<>();
            cartMeals = new ArrayList<>();
            restaurants = new ArrayList<>();
            sushiCategories = new ArrayList<>();
            discountCodes = new ArrayList<>();
            deliveryPrices = new ArrayList<>();
            dbSession.loadMenu();
            dbSession.loadRestaurants();
            dbSession.loadSushiCategories();
            dbSession.loadDiscountCodes();
            dbSession.loadDeliveryPrices();
        }
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else super.onBackPressed();
    }

    public void activityHandwriting(View v) {
        Intent intent = new Intent(this, HandwritingRecognizeActivity.class);
        startActivity(intent);
    }

    public void activityRecognize(View v) {
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivity(intent);
    }

    public void shoppingCart(View v) {
        replaceFragment(new CartFragment());
    }

    public void formFragment(View v) {
        replaceFragment(new FormFragment());
    }

    public void menu(View v) {
        replaceFragment(new MenuFragment());
    }

    public void location(View v) {
        startActivity(new Intent(this, MapsActivity.class));

    }

    public void placeOrder(View v) {
        replaceFragment(new OrderFragment());
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }
}
