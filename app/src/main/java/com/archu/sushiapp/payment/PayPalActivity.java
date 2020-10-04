package com.archu.sushiapp.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.archu.sushiapp.db.DbSession;
import com.archu.sushiapp.db.model.ClientOrderDetails;
import com.archu.sushiapp.mail.GMailSender;
import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.db.model.Order;
import com.archu.sushiapp.R;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static com.archu.sushiapp.MainActivity.cartMeals;
import static com.archu.sushiapp.MainActivity.order;

public class PayPalActivity extends AppCompatActivity {
    private static final int PAYPAL_REQUEST_CODE = 7777;

    private Double amount;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(PayPalConfig.PAYPAL_CLIENT_ID);

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paypal);
        amount = getIntent().getExtras().getDouble("totalPrice");


        //start paypal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        processPayment();
    }

    private void processPayment() {
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(amount), "PLN",
                "Koszt: ", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        startActivity(new Intent(this, PaymentDetails.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("Amount", amount));
                        String timeString = getDeliveryTime();
                        ClientOrderDetails clientOrderDetails = createClientOrderDetails(timeString);
                        sendMail(clientOrderDetails);
                        postClientOrderToDb(clientOrderDetails);
                        cleanupSession();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED)
                Toast.makeText(this, "Wyjście", Toast.LENGTH_SHORT).show();
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID)
            Toast.makeText(this, "Nieprawidłowość", Toast.LENGTH_SHORT).show();
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

    private ClientOrderDetails createClientOrderDetails(String timeString) {
        return new ClientOrderDetails(UUID.randomUUID(), timeString,
                        getIntent().getExtras().getString("email"),
                        getIntent().getExtras().getString("firstName"),
                        getIntent().getExtras().getString("lastName"),
                        getIntent().getExtras().getString("phoneNumber"),
                        getIntent().getExtras().getString("street"),
                        getIntent().getExtras().getString("houseNumber"),
                        getIntent().getExtras().getString("apartmentNumber"),
                        order.toString(), new Date(),
                        true,
                        getIntent().getExtras().getDouble("totalPrice"));
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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private String getDeliveryTime() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getIntent().getExtras().getLong("tsDeliver"));
        return new SimpleDateFormat("HH:mm").format(cal.getTime());
    }
}