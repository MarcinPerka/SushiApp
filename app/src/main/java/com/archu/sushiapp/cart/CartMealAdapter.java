package com.archu.sushiapp.cart;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.db.model.CartMeal;
import com.squareup.picasso.Picasso;

import static android.view.View.inflate;

public class CartMealAdapter extends BaseAdapter {

    private Context context;

    public CartMealAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return MainActivity.cartMeals.size();
    }

    @Override
    public Object getItem(int position) {
        return MainActivity.cartMeals.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflate(context, R.layout.cart_meal, null);
        ImageButton mealBtn = view.findViewById(R.id.mealBtn);
        TextView nameTxt = view.findViewById(R.id.nameTxt);
        TextView priceTxt = view.findViewById(R.id.priceTxt);
        ImageButton addBtn = view.findViewById(R.id.addBtn);
        TextView quantityTxt = view.findViewById(R.id.quantityTxt);
        ImageButton removeBtn = view.findViewById(R.id.removeBtn);

        Picasso.with(getContext())
                .load(MainActivity.cartMeals.get(position).getMeal().getImageLink())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .fit()
                .into(mealBtn);
        nameTxt.setText(MainActivity.cartMeals.get(position).getMeal().getName());
        priceTxt.setText(String.format("%s zł", MainActivity.cartMeals.get(position).getMeal().getPrice().toString()));
        quantityTxt.setText(String.format("%s szt.", MainActivity.cartMeals.get(position).getQuantity().toString()));
        view.setTag(MainActivity.cartMeals.get(position).getMeal().getId());
        addBtn.setOnClickListener(v -> {
            MainActivity.cartMeals.get(position).setQuantity(MainActivity.cartMeals.get(position).getQuantity() + 1);
            notifyDataSetChanged();
            Toast.makeText(context, "Ilość: " + MainActivity.cartMeals.get(position).getQuantity(), Toast.LENGTH_SHORT).show();
        });
        removeBtn.setOnClickListener(v -> {
            CartMeal cartMeal = MainActivity.cartMeals.get(position);
            cartMeal.setQuantity(cartMeal.getQuantity() - 1);
            if (cartMeal.getQuantity() < 1)
                MainActivity.cartMeals.remove(position);
            notifyDataSetChanged();
        });
        return view;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}