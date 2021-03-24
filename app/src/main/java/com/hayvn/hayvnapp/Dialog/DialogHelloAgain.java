package com.hayvn.hayvnapp.Dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.DialogHelloagainBinding;

import java.util.Objects;

public class DialogHelloAgain extends DialogFragment {

    Context context;
    DialogHelloagainBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//onCreateDialog(Bundle savedInstanceState) {

        binding = DialogHelloagainBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        context = getContext();
        builder.setView(binding.getRoot());
        binding.btnOkHelloAgain.setOnClickListener(v -> {
            dismiss();
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams params = Objects.requireNonNull(
                Objects.requireNonNull(getDialog()).getWindow()).getAttributes();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        Objects.requireNonNull(getDialog().getWindow()).setAttributes(params);
        super.onResume();
    }

}
