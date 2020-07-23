package cn.troph.tomon.ui.activities

import com.alibaba.sdk.android.push.AndroidPopupActivity
import com.orhanobut.logger.Logger

class SecondActivity : AndroidPopupActivity() {
    override fun onSysNoticeOpened(p0: String?, p1: String?, p2: MutableMap<String, String>?) {
        Logger.d("push success ${p0} ${p1}")
    }
}