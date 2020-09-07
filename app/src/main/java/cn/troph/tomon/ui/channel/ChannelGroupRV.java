package cn.troph.tomon.ui.channel;

import java.util.Comparator;
import java.util.List;

import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelGroupRV {
    protected GuildChannel mChannel;
    private List<ChannelGroupRV> mChannels;

    private Comparator<ChannelGroupRV> comparator =  new Comparator<ChannelGroupRV>() {
        @Override
        public int compare(ChannelGroupRV o1, ChannelGroupRV o2) {
            return o1.mChannel.getPosition() - o2.mChannel.getPosition();
        }
    };

    public ChannelGroupRV(GuildChannel channel, List<ChannelGroupRV> channels) {
        mChannel = channel;
        mChannels = channels;
    }

    public String getName() {
        return mChannel.getName();
    }

    public List<ChannelGroupRV> getChannels() {
        return mChannels;
    }

    public void addSortedByPostion(ChannelGroupRV channel) {
        mChannels.add(channel);
        mChannels.sort(comparator);
    }

    @Override
    public String toString() {
        String name = "";
        if (mChannel != null) {
            name = mChannel.getName();
        }
        return "ChannelGroupRV{" +
                "mName='" + name + '\'' +
                ", mChannels=" + mChannels +
                '}';
    }
}
