package cn.troph.tomon.ui.states

import cn.troph.tomon.core.utils.event.RxBus

data class ChannelSelection(val guildId: String? = null, val channelId: String? = null)

data class AppUIEvent(val type: AppUIEventType, val value: Any? = null)

enum class AppUIEventType {
    CHANNEL_DRAWER,
    MEMBER_DRAWER
}

class AppState {

    private object HOLDER {
        val INSTANCE = AppState()
    }

    companion object {
        val global: AppState by lazy { HOLDER.INSTANCE }
    }

    val eventBus: RxBus = RxBus()
    val channelSelection: Variable<ChannelSelection> = Variable(ChannelSelection())
    val channelCollapses: Variable<Map<String, Boolean>> = Variable(mapOf())

    fun channelIsCollapsed(channelId: String): Boolean {
        return channelCollapses.value[channelId] ?: false
    }

    fun channelCollapse(channelId: String, collapse: Boolean) {
        val oldMap = channelCollapses.value
        val newMap = oldMap.toMutableMap()
        newMap[channelId] = collapse
        channelCollapses.value = newMap
    }

}