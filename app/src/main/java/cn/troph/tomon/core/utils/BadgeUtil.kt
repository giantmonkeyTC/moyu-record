package cn.troph.tomon.core.utils

object BadgeUtil { //私聊unread工具类
    private val channelUnreadCountMap = HashMap<String, Int>()

    fun clearChannelReadCount(channelId: String) {
        channelUnreadCountMap[channelId] = 0
    }

    fun setChannelUnreadCount(channelId: String, UnreadNumber: Int) {
        channelUnreadCountMap[channelId] = UnreadNumber
    }

    fun increaseChannelUnread(channelId: String) {
        channelUnreadCountMap[channelId]?.let {
            channelUnreadCountMap[channelId] = it + 1
        }
    }

    fun getTotalUnread(): Int {
        var total = 0
        for (item in channelUnreadCountMap) {
            total += item.value
        }
        return total
    }


}