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
import android.widget.Button;
import android.widget.ListView;

import com.hayvn.hayvnapp.Adapter.StoriesPreviewSubmitAdapter;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.ReviewSubmitPreviewBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentReviewSubmit_2 extends Fragment {

    List<StoryFileCount> storiesList = new ArrayList<>();
    ArrayList storiesRaw = new ArrayList();
    StoriesPreviewSubmitAdapter stAdapter;

    // Exchange info with ReviewSubmitActivity
    CallBackFrag2 mCallback;

    private ReviewSubmitPreviewBinding binding;

    public interface CallBackFrag2 {
        public void Update_fr2(String text);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CallBackFrag2) context;
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

        binding = ReviewSubmitPreviewBinding.inflate(inflater,container,false);

        View view = binding.getRoot();



        binding.btnSubmitNext2.setOnClickListener(v -> mCallback.Update_fr2("Clicked Next") );
        binding.btnSubmitBack2.setOnClickListener(v -> mCallback.Update_fr2("Clicked Back") );

        assert getArguments() != null;
        storiesRaw = (getArguments().getParcelableArrayList("edttext"));
        assert storiesRaw != null;
        for(int i = 0; i<storiesRaw.size(); i++){
            storiesList.add((StoryFileCount)storiesRaw.get(i));
        }
        stAdapter = new StoriesPreviewSubmitAdapter(Objects.requireNonNull(this.getContext()));
        stAdapter.setList(storiesList);

       binding.mStories.setAdapter(stAdapter);

        return view;
    }

}


