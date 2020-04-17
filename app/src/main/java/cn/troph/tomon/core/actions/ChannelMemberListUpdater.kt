package cn.troph.tomon.core.actions

import cn.troph.tomon.core.ChannelType
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.core.utils.Collection
import cn.troph.tomon.core.utils.asCollectionOfType

fun getAffectedGuilds(guilds: Collection<Guild>, userId: String): Collection<Guild> {
    return guilds.filter { guild ->
        guild.members[userId] != null
    }
}

fun getAffectedMembers(guild: Guild, role: Role): Collection<GuildMember> {
    return guild.members.filter { member ->
        member.hasRole(role)
    }
}

fun getChannelMemberChanged(channel: Channel, member: GuildMember): Boolean {
    if (channel is GuildChannel) {
        val visible = channel.permissionForMember(member)?.has(Permissions.VIEW_CHANNEL) ?: false
        var changed = false
        if (visible) {
            if (!channel.members.has(member.id)) {
                channel.members.add(member)
                changed = true
            }
        } else {
            if (channel.members.has(member.id)) {
                channel.members.remove(member.id)
                changed = true
            }
        }
        return changed
    }
    return false
}

fun getAffectedChannels(guild: Guild, members: Collection<GuildMember>): Collection<Channel> {
    val channels = guild.channels.filter { channel ->
        channel.type != ChannelType.CATEGORY
    }
    val changes = Collection<Boolean>()
    members.forEach { member ->
        channels.forEach { channel ->
            val changed = getChannelMemberChanged(channel, member)
            if (changed) {
                changes[channel.id] = true
            }
        }
    }
    return channels.filter { channel -> changes[channel.id] ?: false }
        .asCollectionOfType()!!
}

fun getAffectedChannelsForRemoveMembers(
    guild: Guild,
    members: Collection<GuildMember>
): Collection<Channel> {
    val channels = guild.channels.filter { channel ->
        channel.type != ChannelType.CATEGORY
    }
    val changes = Collection<Channel>()
    channels.forEach { channel ->
        members.forEach { member ->
            if (channel.members.has(member.id)) {
                channel.members.remove(member.id)
                changes[channel.id] = channel
            }
        }
    }
    return changes
}