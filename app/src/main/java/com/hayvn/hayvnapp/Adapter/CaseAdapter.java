package com.hayvn.hayvnapp.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hayvn.hayvnapp.Activities.StoryActivity;
import com.hayvn.hayvnapp.Constant.IntentConstants;
import com.hayvn.hayvnapp.Constant.RoomConstants;
import com.hayvn.hayvnapp.Model.Case;
import com.hayvn.hayvnapp.Model.CaseStoryFileCount;
import com.hayvn.hayvnapp.ProdDev.Modes;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.Repository.CaseRepo;
import com.hayvn.hayvnapp.databinding.ItemCaseNamesBinding;

import java.util.ArrayList;
import java.util.List;




public class CaseAdapter extends RecyclerView.Adapter<CaseAdapter.MyViewHolder>{

    private List<CaseStoryFileCount> caseList;
    private Activity context;
    private int position_global;
    public Case case_;


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private ItemCaseNamesBinding binding;

        MyViewHolder(ItemCaseNamesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater inflater = context.getMenuInflater();
            inflater.inflate(R.menu.case_action, menu);
            if(Modes.getSubmission().equals("disabled")){
                menu.findItem(R.id.submit_case_action).setVisible(false);
            }

            //change context menu depending on whether the case was submitted or not
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Case this_case = caseList.get(position_global).getCase_();
            MenuItem submit_case_action = menu.findItem(R.id.submit_case_action);
            MenuItem revoke_submit_case_action = menu.findItem(R.id.revoke_submit_case_action);
            if(this_case.getSubmitted()){
                submit_case_action.setVisible(false);
                revoke_submit_case_action.setVisible(true);
            }else{
                submit_case_action.setVisible(true);
                revoke_submit_case_action.setVisible(false);
            }
        }
    }

    public CaseAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public CaseAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new MyViewHolder(ItemCaseNamesBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    public void setList(List<CaseStoryFileCount> caseList) {
        this.caseList = caseList;
        notifyDataSetChanged();
    }

    private void setPosition(int pos) {
        this.position_global = pos;
    }

    public Case getCase(){
        case_ = caseList.get(position_global).getCase_();
        return case_;
    }

    private ArrayList<String> casesToStringArray() {
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < caseList.size(); i++) {
            out.add(caseList.get(i).getCase_().getName());
        }
        return out;
    }

    private int[] casesIdArray() {
        int[] out = new int[caseList.size()];
        for (int i = 0; i < caseList.size(); i++) {
            out[i] = caseList.get(i).getCase_().getCid();
        }
        return out;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (caseList != null) {
            CaseStoryFileCount csfc = caseList.get(position);
            Case cs = csfc.getCase_();
            holder.binding.caseName.setText(cs.getName());

            holder.binding.tvFiles.setText(String.valueOf(csfc.getFilecount()));
            holder.binding.tvNote.setText(String.valueOf(csfc.getStorycount()));

            String smr = cs.getSummary();
            if(smr != null){
                int lng = smr.length();
                smr = smr.substring(0, Math.min(lng, 100));
                smr = (lng > 100) ? smr.concat("...") : smr;
                holder.binding.tvDes.setText(smr);
            }else{
                holder.binding.tvDes.setText("");
            }

            if (cs.getSubmitted()) {
                holder.binding.folder1.setImageResource(R.drawable.ic_open_folder);
            }

            String st = cs.getStringThree();
            if(st == null || !st.equals(RoomConstants.IS_FAVOURITE_ED)){
                holder.binding.isThisFavCase.setVisibility(View.GONE); //grey

            }else{
                holder.binding.isThisFavCase.setVisibility(View.VISIBLE);
                holder.binding.isThisFavCase.setImageResource(R.drawable.ic_star_blankfavourite_24dp);
            }


            holder.itemView.setOnClickListener(view -> {
                Intent myIntent = new Intent(context, StoryActivity.class);
                myIntent.putExtra(IntentConstants.STORY_ACT_CASE, cs);
                myIntent.putExtra("caseIds", casesIdArray());
                myIntent.putStringArrayListExtra("allCases", casesToStringArray());
                context.startActivityForResult(myIntent, 1);
            });

            // based ont his one https://stackoverflow.com/questions/26466877/how-to-create-context-menu-for-recyclerview
            holder.itemView.setOnLongClickListener(v -> {
                setPosition(position);
                return false;
            });

            holder.binding.btnMore.setOnClickListener(v->{
                setPosition(position);
                v.showContextMenu();
            });

        } else {
            holder.binding.caseName.setText("No cases added");
        }
    }
    @Override
    public void onViewRecycled(MyViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        if (caseList != null)
            return caseList.size();
        else return 0;
    }
}