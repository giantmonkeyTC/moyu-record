package cn.troph.tomon.ui.channel;

import android.content.Context;

import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelRV {

    private Context mContext;
    private GuildChannel mChannel;
    private ChannelGroupRV mParent;
    private boolean mIsVisible = true;

    /**
     * construct a channel node
     * @param channel
     */
    public ChannelRV(Context context, ChannelGroupRV parent, GuildChannel channel) {
        mContext = context;
        mChannel = channel;
        mParent = parent;
    }

    public Context getContext() {
        return mContext;
    }

    public ChannelGroupRV getParent() {
        return mParent;
    }

    public GuildChannel getChannel() {
        return mChannel;
    }

    public void setChannel(GuildChannel channel) {
        mChannel = channel;
    }

    public String getName() {
        return mChannel.getName();
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public void setIsVisible(boolean mIsVisible) {
        this.mIsVisible = mIsVisible;
    }

    @Override
    public String toString() {
        String parentName = "";
        if (mChannel.getParent() != null) {
            parentName = mChannel.getParent().getName();
        }
        return "ChannelRV{" +
                "name=" + mChannel.getName() +
                "\nparent=" + parentName +
                "\ntype=" + mChannel.getType().name() +
                "\nindent=" + mChannel.getIndent() +
                "\nposition=" + mChannel.getPosition() +
                '}';
    }
}
