package com.archu.sushiapp.meal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.archu.sushiapp.db.model.CartMeal;
import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.db.model.Meal;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MealDetailsFragment extends Fragment {

    private ImageView mealImg;
    private TextView nameTxt, priceTxt, descriptionTxt;
    private ImageButton addBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_details, container, false);

        mealImg = view.findViewById(R.id.mealImg);
        nameTxt = view.findViewById(R.id.nameTxt);
        priceTxt = view.findViewById(R.id.priceTxt);
        descriptionTxt = view.findViewById(R.id.descriptionTxt);
        addBtn = view.findViewById(R.id.addBtn);

        Bundle bundle = getArguments();
        if (bundle != null) {
            nameTxt.setText(bundle.getString("name"));
            priceTxt.setText(String.format("%s PLN", bundle.getDouble("price")));
            descriptionTxt.setText(bundle.getString("description"));
            Picasso.with(getContext()).load(bundle.getString("image")).fit().into(mealImg);
        }

        Meal meal = MainActivity.meals.stream().filter(it -> it.getName().equals(Objects.requireNonNull(bundle).getString("name"))).findFirst().get();


        addBtn.setOnClickListener(v -> {
            CartMeal cartMeal = new CartMeal(meal, 1);
            for (CartMeal meal1 : MainActivity.cartMeals) {
                if (meal1.getMeal().getId().equals(cartMeal.getMeal().getId())) {
                    meal1.setQuantity(meal1.getQuantity() + 1);
                    Toast.makeText(getContext(), "Ponownie dodałeś do koszyka, Ilość: " + meal1.getQuantity(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            MainActivity.cartMeals.add(cartMeal);
            Toast.makeText(getContext(), "Dodano do koszyka", Toast.LENGTH_SHORT).show();
        });

        return view;
    }


}
