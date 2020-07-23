package cn.troph.tomon.ui.states

import android.content.Context
import com.alibaba.sdk.android.push.MessageReceiver
import com.alibaba.sdk.android.push.notification.CPushMessage

class TomonMessageReceiver:MessageReceiver() {
    val REC_TAG = "receiver"

    override fun onNotification(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: MutableMap<String, String>?
    ) {
        super.onNotification(p0, p1, p2, p3)
    }

    override fun onMessage(p0: Context?, p1: CPushMessage?) {
        super.onMessage(p0, p1)
    }

    override fun onNotificationOpened(p0: Context?, p1: String?, p2: String?, p3: String?) {
        super.onNotificationOpened(p0, p1, p2, p3)
    }

    override fun onNotificationClickedWithNoAction(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: String?
    ) {
        super.onNotificationClickedWithNoAction(p0, p1, p2, p3)
    }

    override fun onNotificationReceivedInApp(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: MutableMap<String, String>?,
        p4: Int,
        p5: String?,
        p6: String?
    ) {
        super.onNotificationReceivedInApp(p0, p1, p2, p3, p4, p5, p6)
    }

    override fun onNotificationRemoved(p0: Context?, p1: String?) {
        super.onNotificationRemoved(p0, p1)
    }



}