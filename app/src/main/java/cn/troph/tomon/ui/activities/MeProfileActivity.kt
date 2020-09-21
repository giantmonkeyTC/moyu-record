package cn.troph.tomon.ui.activities

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.chat.fragments.LogoutDialogFragment
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import kotlinx.android.synthetic.main.fragment_me.*
import kotlinx.android.synthetic.main.fragment_me.me_bio
import kotlinx.android.synthetic.main.fragment_me.me_name
import kotlinx.android.synthetic.main.me_profile.*

class MeProfileActivity : BaseActivity() {
    private val mChatVM: ChatSharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mChatVM.loadUserInfo()
        setContentView(R.layout.me_profile)
        val me = mChatVM.userInfoLiveData.value
        if (me != null) {
            me_profile_avatar.user = me
            me_profile_name.text = me.name
            me_username.text = me.identifier
        }
        mChatVM.userInfoLiveData.observe(this, Observer {
            it?.let { me ->
                me_profile_avatar.user = me
                me_profile_name.text = me.name
                me_username.text = me.identifier
            }
        })
        back_to_me.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.no_animation, R.anim.slide_out_right_custom)
    }
}