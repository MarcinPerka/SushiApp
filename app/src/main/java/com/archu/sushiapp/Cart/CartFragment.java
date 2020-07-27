package com.archu.sushiapp.Cart;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;

import java.util.Objects;

public class CartFragment extends Fragment {

    private ListView menuListView;
    private CartMealAdapter mealAdapter;
    private TextView cartTxt;
    private Button placeOrderBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_cart, container, false);
        menuListView = view.findViewById(R.id.menuListView);
        mealAdapter = new CartMealAdapter(Objects.requireNonNull(getActivity()).getApplicationContext());
        menuListView.setAdapter(mealAdapter);

        cartTxt = view.findViewById(R.id.cartTxt);
        placeOrderBtn = view.findViewById(R.id.placeOrderBtn);

        setValuesInShoppingCart();

        setAdapterObserver();

        return view;
    }

    private void setValuesInShoppingCart() {
        if (MainActivity.cartMeals.isEmpty()) {
            cartTxt.setText(R.string.cart_empty);
            placeOrderBtn.setVisibility(View.INVISIBLE);
        } else {
            cartTxt.setText(R.string.cart_not_empty);
            placeOrderBtn.setVisibility(View.VISIBLE);

        }
    }

    private void setAdapterObserver() {
        menuListView.getAdapter().registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                setValuesInShoppingCart();
            }
        });
    }

}
