package cn.troph.tomon.ui.channel;

import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelRV {
    protected GuildChannel mChannel;

    /**
     * construct a channel node
     * @param channel
     */
    public ChannelRV(GuildChannel channel) {
        mChannel = channel;
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
