package com.hayvn.hayvnapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.ItemStorySummaryBinding;

import java.util.List;

public class StoriesAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<StoryFileCount> stories;
    private ItemStorySummaryBinding binding;

    public StoriesAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setList(List<StoryFileCount> list) {
        this.stories = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (stories != null)
            return stories.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (stories != null)
            return stories.get(position);
        else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View converView, ViewGroup parent) {

        binding = ItemStorySummaryBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);


        final StoryFileCount cs = (StoryFileCount) getItem(position);

        String storyType = cs.getStory().getTypeEntry();
        String storyStatus = cs.getStory().getStatusEntry();
        if (storyType != null && !storyType.equals("")) {
            if (storyStatus != null && !storyStatus.equals("")) {
                if (Integer.parseInt(storyStatus) < 5) {
                    binding.getRoot().setBackgroundResource(R.drawable.storycard_border_dashed);
                } else {
                    binding.getRoot().setBackgroundResource(R.drawable.storycard_border);
                }
            } else {
                binding.getRoot().setBackgroundResource(R.drawable.storycard_border_dashed);
            }
        } else {
            binding.getRoot().setBackgroundResource(R.drawable.storycard_border);
        }

        long files = cs.getFilecount();
        if (files > 0) {
            binding.numberOfFiles.setText(String.valueOf(files));
        } else {
            binding.numberOfFiles.setVisibility(View.INVISIBLE);
        }

        if(cs.getStory().getOccurredAt() != null){
            binding.dateOfEntry.setText(cs.getStory().getOccurredAt());
        }

        binding.eventListTitle.setText(ifNullEmpty(cs.getStory().getTitle()));
        if (cs.getStory().getLog() != null) {
            String log_ = cs.getStory().getLog();
            int lng = log_.length();
            log_ = (lng > 100) ? (log_.substring(0, 90).concat("...")) : log_;
            binding.eventListSubtitle.setText(log_);
        }

        return binding.getRoot();
    }

    private String ifNullEmpty(String s){
        if(s == null){return "";}
        return s;
    }

}
