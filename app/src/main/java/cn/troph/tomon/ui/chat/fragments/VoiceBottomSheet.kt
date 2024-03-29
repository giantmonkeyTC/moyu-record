package cn.troph.tomon.ui.chat.fragments


import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.GatewayOp
import cn.troph.tomon.core.structures.User
import cn.troph.tomon.core.structures.VoiceChannel
import cn.troph.tomon.core.structures.VoiceConnectSend
import cn.troph.tomon.ui.chat.members.VoiceUserAdapter
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.android.synthetic.main.voice_bottom_sheet.*

class VoiceBottomSheet : BottomSheetDialogFragment() {

    private val mChatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private val mVoiceUserList = mutableListOf<User>()
    private val mAdapter = VoiceUserAdapter(mVoiceUserList)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return LayoutInflater.from(requireContext())
            .inflate(R.layout.voice_bottom_sheet, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val audioManager =
            requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_CURRENT
        button4.isChecked = audioManager.isMicrophoneMute

        button4.setOnCheckedChangeListener { buttonView, isChecked ->
            audioManager.isMicrophoneMute = isChecked
            mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let {
                Client.global.socket.send(
                    GatewayOp.VOICE,
                    Gson().toJsonTree(
                        VoiceConnectSend(
                            it.id,
                            audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                            isChecked
                        )
                    )
                )
            }
            val mp = MediaPlayer.create(requireContext(), R.raw.button_press)
            mp.start()
        }

        // do not change order 1
        button6.setOnCheckedChangeListener { _, isChecked ->
            mChatSharedViewModel.voiceSelfDeafLD.value = isChecked
            val mp = MediaPlayer.create(requireContext(), R.raw.button_press)
            mp.start()
        }

        button7.setOnCheckedChangeListener { buttonView, isChecked ->
            mChatSharedViewModel.voiceLeaveClick.value = true
            mChatSharedViewModel.switchingChannelVoiceLD.value = false
            dismiss()
        }

        //加入或者离开频道
        mChatSharedViewModel.selectedCurrentVoiceChannel.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                dismiss()
                return@Observer
            }

            it.let {
                voice_channel_id.text = "#${it.name}"
                voice_guild_name_tv.text =
                    "${it.guild?.name}"
            }

            it.let {
                mVoiceUserList.clear()
                (it as VoiceChannel).voiceStates.forEach {
                    Client.global.users[it.userId]?.let { user ->
                        if (user.id != Client.global.me.id) {
                            user.isSelfMute = it.isSelfMute
                            user.isSelfDeaf = it.isSelfDeaf
                            mVoiceUserList.add(user)
                        }
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
        })


        //通话更新信息
        mChatSharedViewModel.voiceStateUpdateLD.observe(viewLifecycleOwner, Observer {

            mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let { channel ->
                if (!it.channelId.isNullOrEmpty() && !it.guildId.isNullOrEmpty() && !it.sessionId.isNullOrEmpty() && channel.id == it.channelId) {//add
                    mVoiceUserList.removeIf { user ->
                        user.id == it.userId
                    }
                    Client.global.users[it.userId]?.let { user ->
                        if (user.id != Client.global.me.id) {
                            user.isSelfDeaf = it.isSelfDeaf
                            user.isSelfMute = it.isSelfMute
                            mVoiceUserList.add(user)
                            mAdapter.notifyDataSetChanged()
                        }

                    }
                }
                if (it.channelId.isNullOrEmpty() && it.guildId.isNullOrEmpty()) {//remove
                    mVoiceUserList.removeIf { user ->
                        user.id == it.userId
                    }
                    mAdapter.notifyDataSetChanged()
                }
            }
        })

        //自己说话
        mChatSharedViewModel.voiceSpeakLD.observe(viewLifecycleOwner, Observer { speaking ->

            if (speaking.userId.isNotEmpty()) {
                mVoiceUserList.forEach {
                    it.isSpeaking =
                        (it.id == speaking.userId && speaking.isSpeaking == 1)
                }
                mAdapter.notifyDataSetChanged()
            }
            if (speaking.userId == Client.global.me.id) {
                if (speaking.isSpeaking == 1 && !audioManager.isMicrophoneMute) {
                    voice_myself_state.borderColor = requireContext().getColor(R.color.speaking)
                } else {
                    voice_myself_state.borderColor = requireContext().getColor(R.color.white)
                }
            }
        })

        voice_avatar_rr.layoutManager = LinearLayoutManager(requireContext())
        voice_avatar_rr.adapter = mAdapter
        voice_myself.user = Client.global.me
    }
}