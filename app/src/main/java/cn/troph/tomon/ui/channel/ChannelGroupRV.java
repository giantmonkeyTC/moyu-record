package cn.troph.tomon.ui.channel;

import java.util.List;

import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelGroupRV {
    private String mName;
    private List<ChannelRV> mChannels;

    public ChannelGroupRV(String name, List<ChannelRV> channels) {
        mName = name;
        mChannels = channels;
    }

    public String getName() {
        return mName;
    }

    public List<ChannelRV> getChannels() {
        return mChannels;
    }

    public void add(int index, ChannelRV channel) {
        mChannels.add(index, channel);
    }
}
