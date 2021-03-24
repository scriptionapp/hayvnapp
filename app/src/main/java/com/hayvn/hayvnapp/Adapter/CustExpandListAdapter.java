package com.hayvn.hayvnapp.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import  com.hayvn.hayvnapp.databinding.ItemExpandableElementBinding;
import com.hayvn.hayvnapp.databinding.ItemExpandableHeaderBinding;

import com.hayvn.hayvnapp.R;


public class CustExpandListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;
    private ItemExpandableElementBinding itemExpandableElementBinding;
    private LayoutInflater layoutInflater;
    private ItemExpandableHeaderBinding itemExpandableHeaderBinding;

    public CustExpandListAdapter(Context context, List<String> expandableListTitle,
                                 HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);

            itemExpandableElementBinding = ItemExpandableElementBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);


        itemExpandableElementBinding.expandedListItem.setText(expandedListText);
        return itemExpandableElementBinding.getRoot();
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        itemExpandableHeaderBinding = ItemExpandableHeaderBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        itemExpandableHeaderBinding.listTitle.setTypeface(null, Typeface.BOLD);
       itemExpandableHeaderBinding.listTitle.setText(listTitle);
        return itemExpandableHeaderBinding.getRoot();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

}
