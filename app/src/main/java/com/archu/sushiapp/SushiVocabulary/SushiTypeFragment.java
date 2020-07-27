package com.archu.sushiapp.SushiVocabulary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.archu.sushiapp.R;
import com.squareup.picasso.Picasso;

public class SushiTypeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sushi_type, container, false);

        TextView descriptionTxt = view.findViewById(R.id.descriptionTxt);
        ImageView headerImage = view.findViewById(R.id.headerImage);

        Bundle bundle = getArguments();
        if (bundle != null) {
            descriptionTxt.setText(bundle.getString("description"));
            Picasso.with(getContext()).load(bundle.getString("image")).fit().into(headerImage);
        }

        return view;
    }

}
