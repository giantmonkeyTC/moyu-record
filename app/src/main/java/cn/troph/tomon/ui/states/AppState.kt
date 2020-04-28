package cn.troph.tomon.ui.states

data class ChannelSelection(val guildId: String? = null, val channelId: String? = null)

class AppState {

    private object HOLDER {
        val INSTANCE = AppState()
    }

    companion object {
        val global: AppState by lazy { HOLDER.INSTANCE }
    }

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