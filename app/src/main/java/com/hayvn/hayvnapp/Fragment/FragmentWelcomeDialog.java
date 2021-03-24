package com.hayvn.hayvnapp.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hayvn.hayvnapp.Adapter.Intropager;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.IntroBinding;

import java.util.Objects;

public class FragmentWelcomeDialog extends DialogFragment {

    public interface WelcomeDialogListener {
        void onDialogPositiveClick1();
        void onDialogNegativeClick1();
    }

    WelcomeDialogListener mListener;
    Context context;
    Intropager mAdapter;

    private IntroBinding binding;

    @Override //Dialog
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {//onCreateDialog(Bundle savedInstanceState) {

        binding = IntroBinding.inflate(inflater,container,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        context = getContext();
        View dialogView = binding.getRoot();
        builder.setView(dialogView);
        binding.tabDots.setupWithViewPager(binding.pager, true);
        mAdapter = new Intropager(getChildFragmentManager());
        binding.pager.setAdapter(mAdapter);

        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            public void onPageSelected(int position) {
                if (position == 2) {
                    binding.btnNext.setVisibility(View.GONE);
                    binding.btnStart.setVisibility(View.VISIBLE);
                } else {
                    binding.btnNext.setVisibility(View.VISIBLE);
                    binding.btnStart.setVisibility(View.GONE);
                }
            }
        });


        binding.btnNext.setOnClickListener(v -> {
            int ci = binding.pager.getCurrentItem();
            if (ci == 2) {
                ci = 0;
            } else {
                ci += 1;
            }
            binding.pager.setCurrentItem(ci);
        });


        binding.btnStart.setOnClickListener(v -> {
            mListener = (WelcomeDialogListener) getActivity();
            assert mListener != null;
            mListener.onDialogPositiveClick1();
            dismiss();
        });

        binding.btnSkip.setOnClickListener(v -> {
            mListener = (WelcomeDialogListener) getActivity();
            assert mListener != null;
            mListener.onDialogNegativeClick1();
            dismiss();
        });

        return dialogView;
    }

    @Override
    public void onResume() {
        WindowManager.LayoutParams params = Objects.requireNonNull(
                Objects.requireNonNull(getDialog()).getWindow()).getAttributes();
        params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        Objects.requireNonNull(getDialog().getWindow()).setAttributes(
                params);
        super.onResume();
    }

}
