package cn.troph.tomon.ui.channel;

import android.view.ViewGroup;

import java.util.List;

import tomon.customview.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import tomon.customview.expandablerecyclerview.models.ExpandableGroup;
import tomon.customview.expandablerecyclerview.viewholders.ChildViewHolder;
import tomon.customview.expandablerecyclerview.viewholders.GroupViewHolder;

public class ChannelListAdapter extends ExpandableRecyclerViewAdapter {

    public ChannelListAdapter(List list) {
        super(list);
    }

    @Override
    public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {

    }

    @Override
    public void onBindGroupViewHolder(GroupViewHolder holder, int flatPosition, ExpandableGroup group) {

    }
}
