package cn.troph.tomon.core.actions

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.PermissionOverwriteType
import cn.troph.tomon.core.events.ChannelMemberUpdateEvent
import cn.troph.tomon.core.events.ChannelUpdateEvent
import cn.troph.tomon.core.structures.Channel
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.core.structures.PermissionOverwrites
import cn.troph.tomon.core.utils.Collection
import com.google.gson.JsonElement

class ChannelUpdateAction(client: Client) : Action<Channel>(client) {

    data class OverwriteChange(
        val roles: Collection<PermissionOverwrites>,
        val members: Collection<PermissionOverwrites>
    );

    override fun handle(data: JsonElement?, vararg extras: Any?): Channel? {
        val obj = data!!.asJsonObject
        val channel = client.channels[obj["id"].asString]
        if (channel != null) {
            var oldOverwrites: Collection<PermissionOverwrites> = if (channel is GuildChannel) {
                copyOverwrites(channel.permissionOverwrites)
            } else {
                Collection()
            }
            channel.update(obj)
            client.eventBus.postEvent(ChannelUpdateEvent(channel))
            if (channel is GuildChannel) {
                val newOverwrites = copyOverwrites(channel.permissionOverwrites)
                val change = getPermissionOverwritesChange(oldOverwrites, newOverwrites)
                val members = channel.guild?.members?.filter { member ->
                    return@filter when {
                        change.members[member.id] != null -> true
                        member.rawRoles.any { id -> change.roles[id] != null } -> true
                        change.roles[channel.guild?.id ?: ""] != null -> true
                        else -> false
                    }
                }
                if (channel.guild != null && members != null) {
                    val channels = getAffectedChannels(channel.guild!!, members)
                    channels.forEach { c ->
                        client.eventBus.postEvent(ChannelMemberUpdateEvent(c))
                    }
                }

            }
        }
        return channel
    }

    private fun copyOverwrites(overwrites: Collection<PermissionOverwrites>): Collection<PermissionOverwrites> {
        val collect = Collection<PermissionOverwrites>()
        overwrites.forEach { overwrite ->
            collect.put(overwrite.id, PermissionOverwrites(client, overwrite.raw))
        }
        return collect
    }

    private fun getPermissionOverwritesChange(
        oldOverwrites: Collection<PermissionOverwrites>,
        newOverwrites: Collection<PermissionOverwrites>
    ): OverwriteChange {
        val removed = oldOverwrites.keys.fold(Collection<PermissionOverwrites>(), { map, id ->
            val overwrite = oldOverwrites[id]!!
            if (newOverwrites[id] != null) {
                map[id] = overwrite
            }
            return@fold map
        })
        val addedAndUpdated =
            newOverwrites.keys.fold(Collection<PermissionOverwrites>(), { map, id ->
                val overwrite = newOverwrites[id]!!
                // new
                if (oldOverwrites[id] == null) {
                    map[id] = overwrite
                } else { // updated
                    val old = oldOverwrites[id]
                    if (old?.allow != overwrite.allow ||
                        old?.deny != overwrite.deny
                    ) {
                        map[id] = overwrite
                    }
                }
                return@fold map
            })
        val change = OverwriteChange(
            roles = Collection(),
            members = Collection()
        )
        val classify = { map: Collection<PermissionOverwrites> ->
            map.forEach { overwrite ->
                when (overwrite.type) {
                    PermissionOverwriteType.ROLE -> change.roles.put(overwrite.id, overwrite)
                    PermissionOverwriteType.MEMBER -> change.roles.put(overwrite.id, overwrite)
                }
            }
        }
        classify(removed)
        classify(addedAndUpdated)
        return change
    }

}