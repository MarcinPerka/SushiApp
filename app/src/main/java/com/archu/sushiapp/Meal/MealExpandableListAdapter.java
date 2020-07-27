package com.archu.sushiapp.Meal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.archu.sushiapp.model.CartMeal;
import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.model.SushiCategory;
import com.archu.sushiapp.model.Meal;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class MealExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<SushiCategory> categories;
    private Map<String, List<Meal>> listGroupedByCategories;

    public MealExpandableListAdapter(Context context, List<SushiCategory> categories,
                                     Map<String, List<Meal>> listGroupedByCategories) {
        this.context = context;
        this.categories = categories;
        this.listGroupedByCategories = listGroupedByCategories;
    }

    @Override
    public Meal getChild(int listPosition, int expandedListPosition) {
        return this.listGroupedByCategories.get(this.categories.get(listPosition).getCategory())
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Meal meal = getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.card_meal, null);
        }
        ImageButton mealBtn = convertView.findViewById(R.id.mealBtn);
        TextView nameTxt = convertView.findViewById(R.id.nameTxt);
        TextView priceTxt = convertView.findViewById(R.id.priceTxt);
        ImageButton addBtn = convertView.findViewById(R.id.addBtn);

        Picasso.with(context)
                .load(meal.getImageLink())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .fit()
                .into(mealBtn);
        nameTxt.setText(meal.getName());
        priceTxt.setText(String.format("%s PLN", meal.getPrice().toString()));
        convertView.setTag(meal.getId());
        addBtn.setOnClickListener(v -> {
            CartMeal cartMeal = new CartMeal(meal, 1);
            for (CartMeal element : MainActivity.cartMeals) {
                if (element.getMeal().getId() == cartMeal.getMeal().getId()) {
                    element.setQuantity(element.getQuantity() + 1);
                    Toast.makeText(context, "Ponownie dodałeś do koszyka, Ilość: " + element.getQuantity(), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            MainActivity.cartMeals.add(cartMeal);
            Toast.makeText(context, "Dodano do koszyka", Toast.LENGTH_SHORT).show();
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.listGroupedByCategories.get(this.categories.get(listPosition).getCategory())
                .size();
    }

    @Override
    public SushiCategory getGroup(int listPosition) {
        return this.categories.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.categories.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = getGroup(listPosition).getCategory();
        String basicInfo = getGroup(listPosition).getBasicInfo();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group_alternative, null);
        }
        TextView listTitleTextView = convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setText(listTitle);
        TextView descriptionTextView = convertView.findViewById(R.id.descriptionTxt);
        descriptionTextView.setText(basicInfo);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
