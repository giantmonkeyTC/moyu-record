package cn.troph.tomon.ui.channel;

import android.content.Context;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cn.troph.tomon.core.structures.GuildChannel;
import cn.troph.tomon.ui.utils.GuildUtils;

public class ChannelGroupRV extends ChannelRV {

    private List<ChannelRV> mChildren;
    private boolean mCollapsed = false;

    private Comparator<ChannelRV> comparator =  new Comparator<ChannelRV>() {
        @Override
        public int compare(ChannelRV o1, ChannelRV o2) {
            return o1.getChannel().getPosition() - o2.getChannel().getPosition();
        }
    };

    public ChannelGroupRV(Context context, ChannelGroupRV parent, GuildChannel channel, List<ChannelRV> channels) {
        super(context, parent, channel);
        mChildren = channels;
    }

    public List<ChannelRV> getChildrenChannels() {
        return mChildren;
    }

    public void addSortedByPostion(ChannelRV channel) {
        mChildren.add(channel);
        mChildren.sort(comparator);
    }

    public List<ChannelRV> toggleCollapse() {
        List<ChannelRV> excludeSelf = new ArrayList<>();
        List<ChannelRV> flatten = flatten();
        for (ChannelRV child : flatten) {
            if (child == this) {
                continue;
            }
            excludeSelf.add(child);
            if (child.getChannel().getParent() == getChannel()) {
                child.setIsVisible(isCollapsed());
            } else {
                child.setIsVisible(isCollapsed() && !child.getParent().isCollapsed());
            }
        }
        setCollapsed(!isCollapsed());
        return excludeSelf;
    }

    public void setCollapsed(boolean collapsed) {
        mCollapsed = collapsed;
    }

    public boolean isCollapsed() {
        return mCollapsed;
    }

    public List<ChannelRV> flatten() {
        List<ChannelRV> flattenedChannels = new ArrayList<>();
        if (getChannel() != null) {
            flattenedChannels.add(this);
        }
        for (ChannelRV each : mChildren) {
            if (each instanceof ChannelGroupRV) {
                if (((ChannelGroupRV) each).isCollapsed()
                        || GuildUtils.isCollapsedChannelId(getContext(), each.getChannel().getId())) {
                    flattenedChannels.add(each);
                } else {
                    flattenedChannels.addAll(((ChannelGroupRV) each).flatten());
                }
            } else {
                flattenedChannels.add(each);
            }
        }
        return flattenedChannels;
    }

    @Override
    public String toString() {
        String name = "";
        if (getChannel() != null) {
            name = getChannel().getName();
        }
        return "ChannelGroupRV{" +
                "mName='" + name + '\'' +
                ", mChannels=" + mChildren +
                '}';
    }
}
