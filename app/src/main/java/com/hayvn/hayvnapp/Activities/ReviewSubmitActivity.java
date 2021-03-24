package com.hayvn.hayvnapp.Activities;

import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.hayvn.hayvnapp.Fragment.FragmentReviewSubmit_1;
import com.hayvn.hayvnapp.Fragment.FragmentReviewSubmit_2;
import com.hayvn.hayvnapp.Fragment.FragmentReviewSubmit_3;
import com.hayvn.hayvnapp.Fragment.FragmentReviewSubmit_4;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.ViewModel.StoryViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hayvn.hayvnapp.Constant.IntentConstants.CASE_TO_SUBMIT;
import static com.hayvn.hayvnapp.Constant.IntentConstants.THIS_CASE;

public class ReviewSubmitActivity extends BaseParentActivity
        implements FragmentReviewSubmit_1.CallBackFrag1,
        FragmentReviewSubmit_2.CallBackFrag2,
        FragmentReviewSubmit_3.CallBackFrag3,
        FragmentReviewSubmit_4.CallBackFrag4 {


    ActionBar toolbar;
    public static final int first = 1, second = 2, third = 3, fourth = 4;
    Fragment fragment;
    public Case case_;
    public List<StoryFileCount> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_review_submit);
        toolbar = getSupportActionBar();
        Intent intent = getIntent();
        case_ = (Case) Objects.requireNonNull(intent.getExtras()).getSerializable(CASE_TO_SUBMIT);
        selectFragment(first);
    }

    public void selectFragment(int type) {
        switch (type) {
            case first:
                Bundle bundle = new Bundle();
                bundle.putSerializable(THIS_CASE, case_);
                fragment = new FragmentReviewSubmit_1();
                fragment.setArguments(bundle);
                break;
            case third:
                fragment = new FragmentReviewSubmit_3();
                break;
            case fourth:
                fragment = new FragmentReviewSubmit_4();
                break;
            default:
                return;
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.review_submit_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void launchStories() {
        StoryViewModel mViewModel = ViewModelProviders.of(this).get(StoryViewModel.class);
        mViewModel.getStoriesFileCountByCid(case_.getCid()).observe(this, stories -> {
            assert stories != null;
            mList = stories;

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("edttext", (ArrayList) mList);
            fragment = new FragmentReviewSubmit_2();
            fragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.review_submit_fragment_container, fragment);
            fragmentTransaction.commit();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_close, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_close_view) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void Update_fr1(String text) {
        if (text.equals("Clicked Next")) {
            launchStories();
        }
    }

    @Override
    public void Update_fr2(String text) {
        if (text.equals("Clicked Next")) {
            selectFragment(third);
        } else if (text.equals("Clicked Back")) {
            selectFragment(first);
        }
    }

    @Override
    public void Update_fr3(String text) {
        if (text.equals("Clicked Next")) {
            selectFragment(fourth);
        } else if (text.equals("Clicked Back")) {
            selectFragment(second);
        }
    }

    @Override
    public void Update_from_4(String text) {
        if (text.equals("Clicked Ok")) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.finish();
            startActivity(intent);
        }
    }
}
