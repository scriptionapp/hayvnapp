package com.hayvn.hayvnapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hayvn.hayvnapp.Model.StoryFileCount;
import com.hayvn.hayvnapp.R;

import java.util.List;

public class StoriesPreviewSubmitAdapter  extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<StoryFileCount> storiesFiles;



    public StoriesPreviewSubmitAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setList(List<StoryFileCount> list) {
        this.storiesFiles = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (storiesFiles != null)
            return storiesFiles.size();
        else return 0;
    }

    @Override
    public Object getItem(int position) {
        if (storiesFiles != null)
            return storiesFiles.get(position);
        else return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View converView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View rowView = mInflater.inflate(
                R.layout.item_story_preview_submit, parent, false);

        final StoryFileCount cs = (StoryFileCount) getItem(position);

        TextView titleTextView = rowView.findViewById(R.id.event_list_title);
        TextView subtitleTextView = rowView.findViewById(R.id.event_list_subtitle);
        TextView number_of_files = rowView.findViewById(R.id.number_of_files);
        long files = cs.getFilecount();
        if (files > 0) {
            number_of_files.setText(String.valueOf(files));
        } else {
            TextView number_of_files_icon = rowView.findViewById(R.id.number_of_files_icon);
            number_of_files_icon.setVisibility(View.INVISIBLE);
        }
        titleTextView.setText(cs.getStory().getTitle());
        if (cs.getStory().getLog() != null) {
            String log_ = cs.getStory().getLog();
            if (log_.length() > 150) {
                subtitleTextView.setText(Html.fromHtml(log_.substring(0, 150)));
            } else {
                subtitleTextView.setText(Html.fromHtml(log_));
            }
        }

        return rowView;
    }
}
