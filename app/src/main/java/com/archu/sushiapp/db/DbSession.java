package com.archu.sushiapp.db;

import android.app.Activity;

import com.archu.sushiapp.db.model.Restaurant;
import com.archu.sushiapp.db.model.DiscountCode;
import com.archu.sushiapp.db.model.ClientOrderDetails;
import com.archu.sushiapp.db.model.DeliveryPrice;
import com.archu.sushiapp.db.model.Meal;
import com.archu.sushiapp.db.model.SushiCategory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.archu.sushiapp.MainActivity.deliveryPrices;
import static com.archu.sushiapp.MainActivity.discountCodes;
import static com.archu.sushiapp.MainActivity.meals;
import static com.archu.sushiapp.MainActivity.restaurants;
import static com.archu.sushiapp.MainActivity.sushiCategories;

public class DbSession extends Activity {


    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void loadMenu() {
        db.collection("menu")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        meals = Objects.requireNonNull(task.getResult()).toObjects(Meal.class);
                    }
                });
    }

    public void loadSushiCategories() {
        db.collection("category")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sushiCategories = Objects.requireNonNull(task.getResult()).toObjects(SushiCategory.class);
                    }
                });
    }

    public void loadDiscountCodes() {
        db.collection("discountCode")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        discountCodes = Objects.requireNonNull(task.getResult()).toObjects(DiscountCode.class);
                    }
                });
    }

    public void loadRestaurants() {
        db.collection("restaurant")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            GeoPoint geoPoint = document.getGeoPoint("coordinates");
                            double lat = 0;
                            double lng = 0;
                            if (geoPoint != null) {
                                lat = geoPoint.getLatitude();
                                lng = geoPoint.getLongitude();
                            }
                            LatLng latLng = new LatLng(lat, lng);
                            Restaurant restaurant = new Restaurant(document.getString("street"), latLng);
                            restaurants.add(restaurant);
                        }
                    }
                });
    }

    public static DiscountCode lookForDiscountCode(String text) {
        String finalText = Arrays.stream(text.split(" "))
                .map(String::trim)
                .collect(Collectors.joining(""))
                .replaceAll("[^a-zA-Z0-9_-]", "")
                .toUpperCase();
        for (DiscountCode discountCode : discountCodes) {
            if (finalText.contains(discountCode.getCode()))
                return discountCode;
        }
        return null;
    }

    public void loadDeliveryPrices() {
        db.collection("DeliveryPrice")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        deliveryPrices = Objects.requireNonNull(task.getResult()).toObjects(DeliveryPrice.class);
                    }
                });
    }

    public void useDiscountCode(DiscountCode discountCode) {
        DocumentReference documentReference = db.collection("discountCode").document(discountCode.getCode());

        documentReference
                .update("count", discountCode.getCount() - 1);
    }


    public void addClientOrder(ClientOrderDetails clientOrderDetails) {

        Map<String, Object> user = new HashMap<>();
        user.put("uuid", clientOrderDetails.getUuid().toString());
        user.put("orderDetails", clientOrderDetails.getOrderDetails());
        user.put("timeOfDelivery", clientOrderDetails.getTimeOfDelivery());
        user.put("email", clientOrderDetails.getEmail());
        user.put("firstName", clientOrderDetails.getFirstName());
        user.put("lastName", clientOrderDetails.getLastName());
        user.put("phoneNumber", clientOrderDetails.getPhoneNumber());
        user.put("street", clientOrderDetails.getStreet());
        user.put("houseNumber", clientOrderDetails.getHouseNumber());
        user.put("apartmentNumber", clientOrderDetails.getApartmentNumber());
        user.put("ts", clientOrderDetails.getTs());
        user.put("paid", clientOrderDetails.getPaid());
        user.put("totalPrice", clientOrderDetails.getTotalPrice());

        db.collection("clientOrder")
                .add(user);
    }
}
