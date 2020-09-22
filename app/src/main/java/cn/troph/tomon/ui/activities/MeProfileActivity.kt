package cn.troph.tomon.ui.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import kotlinx.android.synthetic.main.fragment_user_information.view.*
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
            me_profile_name_content.text = me.name
            me_username_content.text = me.identifier
            me_email_content.text =
                if (me.email == null) getString(R.string.profile_empty) else me.email
            me_phone_content.text =
                if (me.phone == null) getString(R.string.profile_empty) else me.phone
            me_profile_name_goto.setOnClickListener {
                val intent = Intent(this, MeSettingsActivity::class.java)
                val bundle = Bundle()
                bundle.putString("property", "name")
                intent.putExtras(bundle)
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right_custom,
                        R.anim.no_animation
                    ).toBundle()
                )
            }
            me_username_goto.setOnClickListener {
                val intent = Intent(this, MeSettingsActivity::class.java)
                val bundle = Bundle()
                bundle.putString("property", "username")
                intent.putExtras(bundle)
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right_custom,
                        R.anim.no_animation
                    ).toBundle()
                )
            }
            me_email_goto.setOnClickListener {
                val intent = Intent(this, MeSettingsActivity::class.java)
                val bundle = Bundle()
                bundle.putString("property", "email")
                intent.putExtras(bundle)
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_right_custom,
                        R.anim.no_animation
                    ).toBundle()
                )
            }
//            me_phone_goto.setOnClickListener {
//                val intent = Intent(this, MeSettingsActivity::class.java)
//                val bundle = Bundle()
//                bundle.putString("property", "phone")
//                intent.putExtras(bundle)
//                startActivity(
//                    intent,
//                    ActivityOptions.makeCustomAnimation(
//                        this,
//                        R.anim.slide_in_right_custom,
//                        R.anim.no_animation
//                    ).toBundle()
//                )
//            }
        }
        mChatVM.userInfoLiveData.observe(this, Observer {
            it?.let { me ->
                me_profile_avatar.user = me
                me_profile_name_content.text = me.name
                me_username_content.text = me.identifier
                me_email_content.text =
                    if (me.email == null) getString(R.string.profile_empty) else me.email
                me_phone_content.text =
                    if (me.phone == null) getString(R.string.profile_empty) else me.phone
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