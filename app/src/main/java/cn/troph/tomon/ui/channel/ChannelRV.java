package cn.troph.tomon.ui.channel;

import java.util.List;

import cn.troph.tomon.core.ChannelType;
import cn.troph.tomon.core.structures.CategoryChannel;
import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelRV extends ChannelGroupRV {

    private GuildChannel mChannel;

    /**
     * construct a channel node
     * @param channel
     */
    public ChannelRV(GuildChannel channel) {
        super(channel.getName(), null);
        mChannel = channel;
    }

    /**
     * construct a channel group node
     * @param channel
     * @param channels
     */
    public ChannelRV(CategoryChannel channel, List<ChannelRV> channels) {
        super(channel.getName(), channels);
        mChannel = channel;
    }

    public boolean isChannelNode() {
        return getChannels() == null && mChannel.getType() != ChannelType.CATEGORY;
    }
}
