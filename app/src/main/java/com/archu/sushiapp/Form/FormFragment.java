package com.archu.sushiapp.Form;

import android.app.AlertDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.model.CartMeal;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

import static com.archu.sushiapp.MainActivity.order;
import static java.util.Comparator.comparingDouble;

public class FormFragment extends Fragment {

    private Geocoder geocoder;
    private EditText editTextFirstName, editTextLastName, editTextEmail,
            editTextStreet, editTextHouseNumber, editTextMobile, editTextApartmentNumber;
    private CircularProgressButton approveBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form, container, false);
        editTextFirstName = view.findViewById(R.id.editTextFirstName);
        editTextLastName = view.findViewById(R.id.editTextLastName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextMobile = view.findViewById(R.id.editTextMobile);
        editTextStreet = view.findViewById(R.id.editTextStreet);
        editTextHouseNumber = view.findViewById(R.id.editTextHouseNumber);
        editTextApartmentNumber = view.findViewById(R.id.editTextApartmentNumber);
        approveBtn = view.findViewById(R.id.approveBtn);
        geocoder = new Geocoder(getContext());
        approveBtn.setOnClickListener(v -> {
            if (validationSuccess())
                createDialog();
            else {
                new AlertDialog.Builder(getContext())
                        .setMessage("Uzupełnij wszystkie pola.")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        return view;
    }

    private boolean validationSuccess() {
        if (editTextFirstName.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextLastName.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextEmail.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextMobile.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextStreet.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextHouseNumber.getText().toString().equalsIgnoreCase("")) {
            return false;
        }
        if (editTextApartmentNumber.getText().toString().equalsIgnoreCase("")) {
            return false;
        }

        return true;
    }

    private void createDialog() {
        List<Address> address = findAddress();
        if (!checkIfLodz(address)) return;
        double distanceInKm = calculateDistanceInKm(address);
        String restaurant = getClosestRestaurantName(address);
        long tsNow = System.currentTimeMillis();
        long tsDrive = TimeUnit.MINUTES.toMillis((long) (distanceInKm * 1));
        long tsMeal = getTsMeal();
        long tsDeliver = getTsDeliver(tsNow, tsDrive, tsMeal);
        if (!checkIfOpen(tsDeliver)) return;

        double priceOfDelivery = MainActivity.deliveryPrices.stream().filter(deliveryPrice -> deliveryPrice.getMaxDistance() >= distanceInKm && distanceInKm >= deliveryPrice.getMinDistance()).findFirst().get().getPrice();
        double price = getTotalPrice(priceOfDelivery);

        StringBuilder stringBuilder = getStringInfoAboutOrder(address, distanceInKm, restaurant, priceOfDelivery, price);
        Bundle bundle = createDialogBundle(tsDeliver, stringBuilder, price);

        AcceptDialogFragment acceptDialogFragment = new AcceptDialogFragment();
        acceptDialogFragment.setArguments(bundle);
        acceptDialogFragment.show(getFragmentManager(), getTag());

    }

    private Bundle createDialogBundle(long tsDeliver, StringBuilder stringBuilder, double totalPrice) {
        Bundle bundle = new Bundle();
        bundle.putLong("tsDeliver", tsDeliver);
        bundle.putDouble("totalPrice", totalPrice);
        bundle.putString("message", stringBuilder.toString());
        bundle.putString("email", editTextEmail.getText().toString());
        bundle.putString("firstName", editTextFirstName.getText().toString());
        bundle.putString("lastName", editTextLastName.getText().toString());
        bundle.putString("phoneNumber", editTextMobile.getText().toString());
        bundle.putString("street", editTextStreet.getText().toString());
        bundle.putString("houseNumber", editTextHouseNumber.getText().toString());
        bundle.putString("apartmentNumber", editTextApartmentNumber.getText().toString());
        return bundle;
    }

    private StringBuilder getStringInfoAboutOrder(List<Address> address, double distanceInKm, String restaurant, double priceOfDelivery, double price) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Odczytany adres: ").append(address.get(0).getAddressLine(0))
                .append("\nNajbliższa restauracja: ").append(restaurant)
                .append("\nDystans do najbliższej restauracji: ").append(distanceInKm).append(" km")
                .append("\nCena dostawy: ").append(priceOfDelivery).append(" zł")
                .append("\nRazem: ").append(price).append(" zł");
        return stringBuilder;
    }

    private double getTotalPrice(double priceOfDelivery) {
        double price;
        order.setDeliveryCost(priceOfDelivery);
        if (order.getDiscountCode() != null)
            price = Math.round((order.getPrice() - (order.getPrice() * order.getDiscountCode().getDiscount()) + priceOfDelivery) * 100.0) / 100.0;
        else
            price = order.getPrice() + priceOfDelivery;
        return price;
    }

    private boolean checkIfOpen(long tsDeliver) {
        if (getRestaurantEndHours().getTime() < tsDeliver || tsDeliver < getRestaurantStartHours().getTime()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.delivery_error_time)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        }
        return true;
    }

    private long getTsDeliver(long tsNow, long tsDrive, long tsMeal) {
        long tsDeliver;
        if (TimeUnit.MILLISECONDS.toMinutes(tsDrive + tsMeal) < 60) {
            tsDeliver = tsNow + TimeUnit.MINUTES.toMillis(60);
        } else {
            tsDeliver = tsNow + tsDrive + tsMeal;
        }
        return tsDeliver;
    }

    private long getTsMeal() {
        long tsMeal = 0;
        for (CartMeal element : order.getCartMeals()) {
            int times = 1;
            if (element.getQuantity() > 4) {
                times += element.getQuantity() / 4;
            }
            tsMeal += TimeUnit.MINUTES.toMillis(20 * times);
        }
        return tsMeal;
    }

    private boolean checkIfLodz(List<Address> address) {
        if (!address.get(0).getAddressLine(0).contains(getString(R.string.delivery_goal))) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.delivery_error)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return false;
        }
        return true;
    }


    private List<Address> findAddress() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(editTextStreet.getText().append(" Łódź, ")
                .append(editTextHouseNumber.getText()).append(" ")
                .append(editTextApartmentNumber.getText()));

        try {
            return geocoder.getFromLocationName(stringBuilder.toString(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Date getRestaurantStartHours() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    private Date getRestaurantEndHours() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }


    private double calculateDistanceInKm(List<Address> result) {
        double addressLongitude = result.get(0).getLongitude();
        double addressLatitude = result.get(0).getLatitude();
        LatLng latLngAddress = new LatLng(addressLatitude, addressLongitude);

        return MainActivity.restaurants.stream().map(restaurant -> getDistance(restaurant.getLatLng(), latLngAddress)).sorted().findFirst().get();

    }

    private String getClosestRestaurantName(List<Address> result) {
        double addressLongitude = result.get(0).getLongitude();
        double addressLatitude = result.get(0).getLatitude();
        LatLng latLngAddress = new LatLng(addressLatitude, addressLongitude);
        List<Double> distances = new ArrayList<>();
        MainActivity.restaurants.stream().forEach(restaurant -> distances.add(getDistance(restaurant.getLatLng(), latLngAddress)));
        int minIndex = IntStream.range(0, distances.size()).boxed().min(comparingDouble(distances::get)).get();
        return MainActivity.restaurants.get(minIndex).getStreet();

    }

    private double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = Math.round((locationA.distanceTo(locationB) / 1000.0) * 100.0) / 100;

        return distance;
    }
}
