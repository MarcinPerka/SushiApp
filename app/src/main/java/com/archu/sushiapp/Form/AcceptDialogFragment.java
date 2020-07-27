package com.archu.sushiapp.Form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.archu.sushiapp.Payment.PaymentFragment;
import com.archu.sushiapp.R;

import java.util.Objects;

public  class AcceptDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (getArguments() != null) {
            builder.setMessage(getArguments().getString("message"))
                    .setPositiveButton("Zamów", (dialog, id) -> {
                        PaymentFragment paymentFragment = new PaymentFragment();
                        paymentFragment.setArguments(getArguments());
                        replaceFragment(paymentFragment);
                    })
                    .setNegativeButton("Wyjdź", (dialog, id) -> {
                        dialog.dismiss();
                    });
        }
        return builder.create();
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
    }
}