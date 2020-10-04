package com.archu.sushiapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.about.AboutUsFragment;
import com.archu.sushiapp.cart.CartFragment;
import com.archu.sushiapp.contact.ContactFragment;
import com.archu.sushiapp.meal.MenuFragment;
import com.archu.sushiapp.maps.MapsActivity;
import com.archu.sushiapp.sushivocabulary.SushiVocabularyFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class MainPanelFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_panel, container, false);

        view.findViewById(R.id.vocabularyBtn).setOnClickListener(this);
        view.findViewById(R.id.locationBtn).setOnClickListener(this);
        view.findViewById(R.id.menuBtn).setOnClickListener(this);
        view.findViewById(R.id.aboutUsBtn).setOnClickListener(this);
        view.findViewById(R.id.shoppingCartBtn).setOnClickListener(this);
        view.findViewById(R.id.contactBtn).setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null && !bundle.isEmpty()) {
            if (Objects.equals(bundle.getString("ordered"), "true"))
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), R.string.mail_sent,
                        Snackbar.LENGTH_LONG)
                        .show();
            bundle.clear();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.vocabularyBtn:
                replaceFragment(new SushiVocabularyFragment());
                break;
            case R.id.aboutUsBtn:
                replaceFragment(new AboutUsFragment());
                break;
            case R.id.menuBtn:
                replaceFragment(new MenuFragment());
                break;
            case R.id.shoppingCartBtn:
                replaceFragment(new CartFragment());
                break;
            case R.id.contactBtn:
                replaceFragment(new ContactFragment());
                break;
            case R.id.locationBtn:
                startActivity(new Intent(getActivity(), MapsActivity.class));
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }
}
