package com.archu.sushiapp.sushivocabulary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.MainActivity;
import com.archu.sushiapp.R;
import com.archu.sushiapp.db.model.SushiCategory;

import java.util.Objects;

public class SushiVocabularyFragment extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sushi_vocabulary, container, false);
        view.findViewById(R.id.makiBtn).setOnClickListener(this);
        view.findViewById(R.id.futomakiBtn).setOnClickListener(this);
        view.findViewById(R.id.nigiriBtn).setOnClickListener(this);
        view.findViewById(R.id.uramakiBtn).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Fragment fragment = new SushiTypeFragment();
        Bundle bundle = new Bundle();
        SushiCategory sushiCategory;
        switch (id) {
            case R.id.makiBtn:
                sushiCategory = MainActivity.sushiCategories.stream().filter(it -> it.getCategory().equals("MAKI")).findFirst().get();
                break;
            case R.id.futomakiBtn:
                sushiCategory = MainActivity.sushiCategories.stream().filter(it -> it.getCategory().equals("FUTOMAKI")).findFirst().get();
                break;
            case R.id.nigiriBtn:
                sushiCategory = MainActivity.sushiCategories.stream().filter(it -> it.getCategory().equals("NIGIRI")).findFirst().get();
                break;
            case R.id.uramakiBtn:
                sushiCategory = MainActivity.sushiCategories.stream().filter(it -> it.getCategory().equals("URAMAKI")).findFirst().get();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + id);
        }
        bundle.putString("description", sushiCategory.getDescription());
        bundle.putString("image", sushiCategory.getImageVocabulary());
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }
}
