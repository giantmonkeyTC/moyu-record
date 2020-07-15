package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.GuildMemberService
import cn.troph.tomon.ui.chat.viewmodel.UserInfoViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.guild_user_info.*

class GuildUserInfoFragment(private val userId: String) : BottomSheetDialogFragment() {
    private val mUserVM: UserInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return LayoutInflater.from(requireContext())
            .inflate(R.layout.guild_user_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mUserVM.loadGuildUserInfo(userId)
        mUserVM.guildUserInfoLD.observe(viewLifecycleOwner, Observer { user ->
            user_info_avatar.user = user
            user_info_name.text = user.name
            user_info_nick.text = "${user.username} #${user.discriminator}"
            user_sign_out.setOnClickListener {
                dismiss()
                ReportFragment(
                    userId,
                    1
                ).show((view.context as AppCompatActivity).supportFragmentManager, null)
            }

            user_private_chat.setOnClickListener {
                Toast.makeText(requireContext(), R.string.no_pchat_support, Toast.LENGTH_SHORT)
                    .show()
            }
        })


    }


}