package com.hayvn.hayvnapp.Adapter;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;
import com.hayvn.hayvnapp.databinding.ItemCommentBinding;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CommentsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<Map<String, String>> comments;
    private final String TAG = "COMMENTADAPTER";

    private ItemCommentBinding binding;

    public CommentsAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setList(List<Map<String, String>> list) {
        this.comments = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (comments != null)
            return comments.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (comments != null)
            return comments.get(position);
        else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View converView, ViewGroup parent) {
        binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        final Map<String, String> comment = (Map<String, String>) getItem(position);
        String upd = comment.get("updatedAt");
        Calendar cal = Calendar.getInstance();
        String date = "";

        try{
            assert upd != null;
            cal.setTimeInMillis(Long.parseLong(upd));
            date = DateFormat.format("yyyy-MM-dd", cal).toString();
        }catch(Exception e){
            Log.d(TAG, e.toString());
        }


        String files = comment.get("file_count");
        if (!files.equals("0") && !files.equals("")) {
            binding.numberOfFiles.setText(files);
        } else {
            binding.numberOfFiles.setVisibility(View.GONE);
        }

        if (!date.equals("0") && !date.equals("")) {
            binding.commentDate.setText(date);
        } else {
            binding.commentDate.setVisibility(View.GONE);
        }
        String TAG = "COMMENT_ADAPTER";
        if (!comment.get("text").equals("")) {
            binding.commentContainer.setBackgroundResource(R.drawable.comment_border_light);
            binding.eventListTitle.setVisibility(View.GONE);
            binding.commentTextView.setText(comment.get("text"));
            binding.commentContainer.setPadding(50,0,0,0);
        } else {
            binding.getRoot().setBackgroundResource(R.drawable.storycard_border);
            binding.eventListTitle.setText(comment.get("story_title").toString());
            binding.commentContainer.setPadding(10,0,0,0);
        }
        return binding.getRoot();
    }

}
