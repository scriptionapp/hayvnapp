package com.hayvn.hayvnapp.Fragment;

import android.app.Activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.hayvn.hayvnapp.Activities.ReviewSubmitActivity;
import com.hayvn.hayvnapp.Adapter.CaseAdapter;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.databinding.ReviewSubmitConfirmedBinding;

import java.util.List;

public class FragmentReviewSubmit_4 extends Fragment {

    public CaseAdapter adapter;
    List<Case> mList;
    EditText searchText;
    boolean toSearch = false;

    CallBackFrag4 mCallback;

    private ReviewSubmitConfirmedBinding binding;

    public interface CallBackFrag4 {
        public void Update_from_4(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CallBackFrag4) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement CallBackFrag4");
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

        binding = ReviewSubmitConfirmedBinding.inflate(inflater,container,false);

        View view = binding.getRoot();


        binding.btnUnderstoodOk.setOnClickListener(v -> mCallback.Update_from_4("Clicked Ok"));
        return view;
    }

}


