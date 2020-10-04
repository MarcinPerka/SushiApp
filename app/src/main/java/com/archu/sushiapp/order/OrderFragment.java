package com.archu.sushiapp.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.archu.sushiapp.R;

import java.util.Objects;

import static com.archu.sushiapp.MainActivity.cartMeals;
import static com.archu.sushiapp.MainActivity.order;

public class OrderFragment extends Fragment {

    private ListView menuListView;
    private OrderMealAdapter mealAdapter;
    private TextView sumTxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_order, container, false);
        menuListView = view.findViewById(R.id.menuListView);
        mealAdapter = new OrderMealAdapter(Objects.requireNonNull(getActivity()).getApplicationContext());
        menuListView.setAdapter(mealAdapter);
        sumTxt = view.findViewById(R.id.sumTxt);
        order.setPrice(cartMeals.stream().map(it -> it.getQuantity() * it.getMeal().getPrice()).mapToDouble(it -> it).sum());
        order.setCartMeals(cartMeals);

        double price;

        if (order.getDiscountCode() != null)
            price = Math.round((order.getPrice() - (order.getPrice() * order.getDiscountCode().getDiscount())) * 100.0) / 100.0;
        else
            price = order.getPrice();
        sumTxt.setText(String.format("Kwota zamówienia: %s zł", price));
        return view;
    }
}