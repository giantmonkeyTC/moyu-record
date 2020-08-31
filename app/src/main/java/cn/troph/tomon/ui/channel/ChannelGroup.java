package cn.troph.tomon.ui.channel;

import java.util.List;

import cn.troph.tomon.core.structures.GuildChannel;
import tomon.customview.expandablerecyclerview.models.ExpandableGroup;

public class ChannelGroup extends ExpandableGroup<GuildChannel> {

    public ChannelGroup(String title, List<GuildChannel> items) {
        super(title, items);
    }

}
