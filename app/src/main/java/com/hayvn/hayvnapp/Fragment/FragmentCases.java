package com.hayvn.hayvnapp.Fragment;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hayvn.hayvnapp.Activities.ReviewSubmitActivity;
import com.hayvn.hayvnapp.Activities.SummaryActivity;
import com.hayvn.hayvnapp.Adapter.CaseAdapter;
import com.hayvn.hayvnapp.Dialog.DialogView;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.CaseStoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.Utilities.Utilities;
import com.hayvn.hayvnapp.ViewModel.CaseViewModel;
import org.jetbrains.annotations.NotNull;
import com.hayvn.hayvnapp.databinding.FragmentCaseBinding;


import static com.hayvn.hayvnapp.Constant.Constant.MODIFY_SUMMARY;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CASE_TO_MODIFY;
import static com.hayvn.hayvnapp.Constant.IntentConstants.CASE_TO_SUBMIT;
import static com.hayvn.hayvnapp.Constant.TextConstants.ENTER_NAME;
import static com.hayvn.hayvnapp.Constant.TextConstants.NO_FAV_PATIENTS;
import static com.hayvn.hayvnapp.Constant.TextConstants.SURE_DELETE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentCases extends Fragment {

    private static final String TAG = "FRAG_CASE";
    public CaseAdapter adapter;
    List<CaseStoryFileCount> mList;
    boolean toSearch = false;
    MutableLiveData<String> direction;

    private FragmentCaseBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentCaseBinding.inflate(inflater,container,false);

        View view = binding.getRoot();
        Objects.requireNonNull(getActivity()).setTitle(getString(R.string.app_name));
        direction = new MutableLiveData();
        direction.setValue("");
        adapter = new CaseAdapter(getActivity());
        CaseViewModel mViewModel = ViewModelProviders.of(this).get(CaseViewModel.class);
        binding.rvCase.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCase.setAdapter(adapter);
        setHasOptionsMenu(true);
        Transformations.switchMap(
                direction,  input -> {
                    if(direction.getValue().equals("")){
                        return mViewModel.getCasesCountsForCase_vm();
                    }else{
                        return mViewModel.getCasesCountsForCase_filtered(subQuery()); //direction.getValue());
                    }
        }).observe(Objects.requireNonNull(getActivity()), caseNames -> {
            assert caseNames != null;
            mList = new ArrayList<CaseStoryFileCount>(caseNames);
            adapter.setList(mList);
            if (mList.size() == 0) {
                binding.buttonNewCaseBig.setVisibility(View.VISIBLE);
                binding.noCasesHint.setVisibility(View.VISIBLE);
                binding.noCasesHint.setText(NO_FAV_PATIENTS);
            } else {
                binding.buttonNewCaseBig.setVisibility(View.INVISIBLE);
                binding.noCasesHint.setVisibility(View.GONE);
            }
        });

        binding.buttonNewCaseBig.setOnClickListener(v -> DialogView.getInstance().showCreateDialog(getActivity(), getString(R.string.add_the_case)));
        binding.edtSearchCase.setVisibility(View.GONE);
        binding.edtSearchCase.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }
            @Override
            public void afterTextChanged(Editable s) {
                setDirection(Utilities.getInstance().getString(binding.edtSearchCase));
            }
        });
        return view;
    }

    private SimpleSQLiteQuery subQuery(){
        String all_filters = direction.getValue();
        String [] all_str = all_filters.split("[\\s,]+");
        String quer = "";
        int i = 0;
        for(i =0; i<all_str.length; i++){
            String s = all_str[i];
            all_str[i] = "(case1.name LIKE '%"+s+"%' OR case1.patientId LIKE '%"+s+"%' OR case1.dateofbirth LIKE '%"+s+"%')";
        }
        quer = all_str[0];
        for(i =1; i<all_str.length; i++){
            quer += " AND "+all_str[i];
        }

        SimpleSQLiteQuery sql_query = new SimpleSQLiteQuery("SELECT *," +
                "(select COUNT(*) from Story as a where a.cid=case1.cid) as storycount, " +
                "(select COUNT(*) from attachedfile as file1 where file1.cid=case1.cid) as filecount " +
                "FROM `case` "+
                "as case1 "+
                "WHERE  "+quer +
                " order by Datetime(updatedAt) desc");
        return sql_query;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mList == null || mList.size() == 0) {
            binding.buttonNewCaseBig.setVisibility(View.VISIBLE);
            binding.noCasesHint.setText(NO_FAV_PATIENTS);
        } else {
            binding.buttonNewCaseBig.setVisibility(View.INVISIBLE);
            binding.noCasesHint.setVisibility(View.GONE);
        }
    }

    private void setDirection(String st) {
        if(st==null) st = "";
        this.direction.setValue(st);
    }

    //this comes from CaseAdapter
    @Override
    public boolean onContextItemSelected(@NotNull MenuItem item) {
        Case case_;
        try {
            case_ = adapter.getCase();
        } catch (Exception e) {
            return super.onContextItemSelected(item);
        }
        Context context = getContext();
        switch (item.getItemId()) {
            case R.id.submit_case_action:
                Intent myIntent_ = new Intent(context, ReviewSubmitActivity.class);
                myIntent_.putExtra(CASE_TO_SUBMIT, case_);
                startActivity(myIntent_);
                break;
            case R.id.revoke_submit_case_action:
                DialogView.getInstance().showRevokeSubmission(getActivity(), case_);
                break;
            case R.id.delete_case_action:
                AlertDel(case_);
                break;
            case R.id.modify_smr_case_action:
                Intent myIntent = new Intent(context, SummaryActivity.class);
                myIntent.putExtra(CASE_TO_MODIFY, case_);
                startActivityForResult(myIntent, MODIFY_SUMMARY);
                break;
            case R.id.rename_case_action:
                DialogView.getInstance().showRenameCaseDialog(getActivity(),
                        ENTER_NAME, case_);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
    }

    private void AlertDel(Case to_del) {
        final AlertDialog alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext())).create();
        alertDialog.setTitle(getString(R.string.alert));
        alertDialog.setMessage(SURE_DELETE);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                getString(R.string.cancel), (dialogInterface, i) -> alertDialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                getString(R.string.okok), (dialog, which) -> {
            alertDialog.dismiss();
            CaseRepo mRepository = new CaseRepo(
                    (Objects.requireNonNull(getActivity())).getApplication());
            mRepository.delete(to_del);
        });
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                if(toSearch){
                    toSearch = false;
                    binding.edtSearchCase.setText("");
                    binding.edtSearchCase.setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_search_black);
                }else{
                    toSearch = true;
                    binding.edtSearchCase.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_clear_black);
                }

                break;
        }

        return false;
    }

}


