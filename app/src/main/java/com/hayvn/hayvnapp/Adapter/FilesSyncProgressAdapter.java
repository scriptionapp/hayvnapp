package com.hayvn.hayvnapp.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hayvn.hayvnapp.Model.FileNameProgress;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.ItemFilesSyncProgressBinding;

import java.util.List;


public class FilesSyncProgressAdapter extends RecyclerView.Adapter<FilesSyncProgressAdapter.MyViewHolder>{

    private List<FileNameProgress> fileList;
    private Activity mContext;

    class MyViewHolder extends RecyclerView.ViewHolder{
        private ItemFilesSyncProgressBinding binding;

        MyViewHolder(ItemFilesSyncProgressBinding binding) {
            super(binding.getRoot());
            this.binding= binding;
        }
    }

    public FilesSyncProgressAdapter() {

    }

    @NonNull
    @Override
    public FilesSyncProgressAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(ItemFilesSyncProgressBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    public void setList(List<FileNameProgress> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (fileList != null) {
            FileNameProgress csfc = fileList.get(position);
            holder.binding.filename.setText(csfc.getName());
            if(csfc.getProgress().toLowerCase().equals("failed")){
                holder.binding.fileProgress.setText(csfc.getProgress());
            }else{
                holder.binding.fileProgress.setText(csfc.getProgress() + "%");
            }
        } else {
            holder.binding.filename.setText("No files to sync");
        }
    }

    @Override
    public int getItemCount() {
        if (fileList != null)
            return fileList.size();
        else return 0;
    }
}