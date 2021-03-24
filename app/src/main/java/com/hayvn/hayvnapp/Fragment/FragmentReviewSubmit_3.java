package com.hayvn.hayvnapp.Fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.hayvn.hayvnapp.Activities.ReviewSubmitActivity;
import com.hayvn.hayvnapp.Adapter.CaseAdapter;
import com.hayvn.hayvnapp.Interfaces.CallbackObjectUpdated;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.ObjectContainer;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.databinding.ReviewSubmitConfirmBinding;

import java.util.Date;
import java.util.Objects;

public class FragmentReviewSubmit_3 extends Fragment implements CallbackObjectUpdated {

    public CaseAdapter adapter;
    Case case_;
    CaseRepo caseRepo;
    ProgressBar progressBar;
    CallBackFrag3 mCallback;

    private ReviewSubmitConfirmBinding binding;

    @Override
    public void onObjectUpdatedLocally(ObjectContainer obj) {
        //this is for CallbackCaseUpdated
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onObjectGotFireId(ObjectContainer obj, boolean internet_was_avail) {
        //from CallbackCaseUpdated; not required here
    }

    public interface CallBackFrag3 {
        public void Update_fr3(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CallBackFrag3) context;
            caseRepo = new CaseRepo(getActivity().getApplication());
            case_ = ((ReviewSubmitActivity)getActivity()).case_;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CallBackFrag3");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = ReviewSubmitConfirmBinding.inflate(inflater,container,false);
        View view = binding.getRoot();

        binding.btnSubmitBack3.setOnClickListener(v -> mCallback.Update_fr3("Clicked Back") );
        binding.btnSubmitConfirm.setOnClickListener(v -> {
            progressBar = new ProgressBar(super.getContext(), null, android.R.attr.progressBarStyleLarge);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            binding.forProgressbar.addView(progressBar, params);
            int col = ContextCompat.getColor(super.getContext(), R.color.colorPrimary);
            progressBar.setProgressTintList(ColorStateList.valueOf(col));
            progressBar.setVisibility(View.VISIBLE);
            Long now = new Date().getTime();
            case_.setSubmitted(true);
            case_.setSubmittedAt(now);
            caseRepo.update(case_, true, this);
            mCallback.Update_fr3("Clicked Next");
        } );
        return view;
    }

}


