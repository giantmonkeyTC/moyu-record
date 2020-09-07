package cn.troph.tomon.ui.channel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.troph.tomon.R;
import cn.troph.tomon.core.ChannelType;

public class ChannelListAdapter extends RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder> {

    private List<ChannelRV> mDataList = new ArrayList<>();

    public ChannelListAdapter(ChannelGroupRV root) {
        if (root != null) {
            mDataList = root.flatten();
        }
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChannelViewHolder viewHolder = null;
        if (viewType == ChannelType.CATEGORY.getValue()) {
            View groupView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel_category, parent, false);
            viewHolder = new ChannelGroupViewHolder(groupView);
        } else {
            View channelView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel_list, parent, false);
            viewHolder = new ChannelViewHolder(channelView);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).mChannel.getType().getValue();
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {

        ImageView ivUnreadDotStroke;
        ImageView ivUnreadDotContent;
        ImageView ivChannelIcon;
        TextView tvChannelName;
        TextView tvChannelTime;
        TextView tvChannelDesp;


        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUnreadDotStroke = itemView.findViewById(R.id.iv_unread_dot_stroke);
            ivUnreadDotContent = itemView.findViewById(R.id.iv_unread_dot_content);
            ivChannelIcon = itemView.findViewById(R.id.iv_channel);
            tvChannelName = itemView.findViewById(R.id.tv_channel_name);
            tvChannelDesp = itemView.findViewById(R.id.tv_channel_dscp);
            tvChannelTime = itemView.findViewById(R.id.tv_channel_time);
        }
    }

    static class ChannelGroupViewHolder extends ChannelViewHolder {

        ImageView ivCategoryExpandStateIcon;
        TextView tvCategoryName;

        public ChannelGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryExpandStateIcon = itemView.findViewById(R.id.iv_category_expand_state);
            tvCategoryName = itemView.findViewById(R.id.tv_categoty_name);
        }

        public void setUnreadView(boolean unread, boolean mention) {
            if (unread) {
                ivUnreadDotStroke.setVisibility(View.VISIBLE);
                ivUnreadDotContent.setVisibility(View.VISIBLE);
                if (mention) {
                    ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content_alert);
                } else {
                    ivUnreadDotContent.setBackgroundResource(R.drawable.shape_unread_dot_content);
                }
            } else {
                ivUnreadDotStroke.setVisibility(View.INVISIBLE);
                ivUnreadDotContent.setVisibility(View.INVISIBLE);
            }
        }
    }
}
