package com.archu.sushiapp.Meal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.model.SushiCategory;
import com.archu.sushiapp.model.Meal;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MenuFragment extends Fragment {

    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<SushiCategory> categories;
    private Map<String, List<Meal>> listGroupedByCategories;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_menu, container, false);
        expandableListView = view.findViewById(R.id.expandableListView);
        listGroupedByCategories = MainActivity.meals.stream().collect(Collectors.groupingBy(Meal::getCategory));
        categories = MainActivity.sushiCategories;
        expandableListAdapter = new MealExpandableListAdapter(getContext(), categories, listGroupedByCategories);


        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            MealDetailsFragment mealDetailsFragment = new MealDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("name", listGroupedByCategories.get(categories.get(groupPosition).getCategory()).get(childPosition).getName());
            bundle.putString("image", listGroupedByCategories.get(categories.get(groupPosition).getCategory()).get(childPosition).getImageLink());
            bundle.putString("description", listGroupedByCategories.get(categories.get(groupPosition).getCategory()).get(childPosition).getDescription());
            bundle.putDouble("price", listGroupedByCategories.get(categories.get(groupPosition).getCategory()).get(childPosition).getPrice());
            mealDetailsFragment.setArguments(bundle);
            FragmentTransaction transaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
            transaction.replace(R.id.fragmentContainer, mealDetailsFragment).addToBackStack(null).commit();
            return false;
        });

        return view;
    }

}
