package com.archu.sushiapp.Order;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.squareup.picasso.Picasso;

public class OrderMealAdapter extends BaseAdapter {

    private Context context;

    public OrderMealAdapter(Context context) {
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
        View view = View.inflate(context, R.layout.ordered_meal, null);
        ImageButton mealBtn = view.findViewById(R.id.mealBtn);
        TextView nameTxt = view.findViewById(R.id.nameTxt);
        TextView priceTxt = view.findViewById(R.id.priceTxt);
        TextView quantityTxt = view.findViewById(R.id.quantityTxt);

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
        return view;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}