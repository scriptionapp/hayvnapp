package com.hayvn.hayvnapp.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.databinding.ReviewSubmitFirstBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.IntentConstants.THIS_CASE;
import static com.hayvn.hayvnapp.Constant.TextConstants.SEND_WAIT;

public class FragmentReviewSubmit_1 extends Fragment {

    // Exchange info with ReviewSubmitActivity
    CallBackFrag1 mCallback;
    Case case_;
    CaseRepo caseRepo;
    String original_summary, new_summary;

    private ReviewSubmitFirstBinding binding;

    public interface CallBackFrag1 {
        public void Update_fr1(String text);
    }
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        try {
            mCallback = (CallBackFrag1) context;
            caseRepo = new CaseRepo(Objects.requireNonNull(getActivity()).getApplication());
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CallBackFrag1");
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

        binding = ReviewSubmitFirstBinding.inflate(inflater,container,false);

        View view = binding.getRoot();

        assert getArguments() != null;
        case_ = (Case)Objects.requireNonNull(
                getArguments().getSerializable(THIS_CASE) );
        original_summary = case_.getSummary();
        if(original_summary == null){original_summary = "";}
        binding.summaryEdtTxt.setText(case_.getSummary());
        binding.btnSubmitNext1.setOnClickListener(v -> {
            new_summary = binding.summaryEdtTxt.getText().toString();
            if(!original_summary.equals(new_summary)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setMessage(SEND_WAIT)
                        .setTitle("...");
                AlertDialog mWaitingDialog = builder.create();
                mWaitingDialog.show();

                case_.setSummary(new_summary);
                case_ = caseRepo.setUpdateCreate(case_, true, true);
                caseRepo.update(case_);
                mWaitingDialog.dismiss();
            }
            mCallback.Update_fr1("Clicked Next");
        });
        return view;
    }



}


