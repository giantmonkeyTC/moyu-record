package cn.troph.tomon.ui.channel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cn.troph.tomon.core.structures.GuildChannel;

public class ChannelGroupRV extends ChannelRV {

    private List<ChannelRV> mChannels;

    private Comparator<ChannelRV> comparator =  new Comparator<ChannelRV>() {
        @Override
        public int compare(ChannelRV o1, ChannelRV o2) {
            return o1.mChannel.getPosition() - o2.mChannel.getPosition();
        }
    };

    public ChannelGroupRV(GuildChannel channel, List<ChannelRV> channels) {
        super(channel);
        mChannels = channels;
    }

    public String getName() {
        return mChannel.getName();
    }

    public List<ChannelRV> getChannels() {
        return mChannels;
    }

    public void addSortedByPostion(ChannelRV channel) {
        mChannels.add(channel);
        mChannels.sort(comparator);
    }

    public List<ChannelRV> flatten() {
        List<ChannelRV> flattenedChannels = new ArrayList<>();
        if (mChannel != null) {
            flattenedChannels.add(this);
        }
        for (ChannelRV each : mChannels) {
            if (each instanceof ChannelGroupRV) {
                flattenedChannels.addAll(((ChannelGroupRV) each).flatten());
            } else {
                flattenedChannels.add(each);
            }
        }
        return flattenedChannels;
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
