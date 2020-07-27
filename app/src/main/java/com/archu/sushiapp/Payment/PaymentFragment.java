package com.archu.sushiapp.Payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.Db.DbSession;
import com.archu.sushiapp.Mail.GMailSender;
import com.archu.sushiapp.MainPanelFragment;
import com.archu.sushiapp.R;
import com.archu.sushiapp.model.ClientOrderDetails;
import com.archu.sushiapp.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static com.archu.sushiapp.MainActivity.cartMeals;
import static com.archu.sushiapp.MainActivity.order;

public class PaymentFragment extends Fragment {

    private Button onDeliveryBtn, paypalBtn;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        onDeliveryBtn = view.findViewById(R.id.onDeliveryBtn);
        paypalBtn = view.findViewById(R.id.paypalBtn);

        paypalBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayPalActivity.class);
            intent.putExtras(getArguments());
            startActivity(intent);

        });
        onDeliveryBtn.setOnClickListener(v -> {
            String timeString = getDeliveryTime();
            ClientOrderDetails clientOrderDetails = createClientOrderDetails(timeString);
            sendMail(clientOrderDetails);
            postClientOrderToDb(clientOrderDetails);
            cleanupSession();
        });
        return view;
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }

    private ClientOrderDetails createClientOrderDetails(String timeString) {
        return new ClientOrderDetails(UUID.randomUUID(), timeString,
                getArguments().getString("email"),
                getArguments().getString("firstName"),
                getArguments().getString("lastName"),
                getArguments().getString("phoneNumber"),
                getArguments().getString("street"),
                getArguments().getString("houseNumber"),
                getArguments().getString("apartmentNumber"),
                order.toString(), new Date(),
                false,
                getArguments().getDouble("totalPrice")
        );
    }

    private void postClientOrderToDb(ClientOrderDetails clientOrderDetails) {
        DbSession dbSession = new DbSession();
        if (order.getDiscountCode() != null)
            dbSession.useDiscountCode(order.getDiscountCode());
        dbSession.addClientOrder(clientOrderDetails);
    }

    private void cleanupSession() {
        order = new Order();
        cartMeals = new ArrayList<>();
        Bundle bundle = new Bundle();
        bundle.putString("ordered", "true");
        MainPanelFragment mainPanelFragment = new MainPanelFragment();
        mainPanelFragment.setArguments(bundle);
        replaceFragment(mainPanelFragment);
    }

    private void sendMail(ClientOrderDetails clientOrderDetails) {
        GMailSender gMailSender = new GMailSender(getString(R.string.username_mail), getString(R.string.password_mail));
        try {
            gMailSender.sendMail("Zamówienie: " + clientOrderDetails.getUuid(),
                    "Cześć,\ntwoje zamówienie:\n" + clientOrderDetails.getOrderDetails() + "\nCzas dowozu: " + clientOrderDetails.getTimeOfDelivery(), getString(R.string.sender_mail), clientOrderDetails.getEmail())
            ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDeliveryTime() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getArguments().getLong("tsDeliver"));
        return new SimpleDateFormat("HH:mm").format(cal.getTime());
    }


}
