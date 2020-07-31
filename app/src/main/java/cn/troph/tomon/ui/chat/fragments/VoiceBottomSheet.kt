package cn.troph.tomon.ui.chat.fragments


import android.content.Context
import android.media.AudioManager
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
        button6.isChecked = audioManager.isSpeakerphoneOn

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
        }

        button6.setOnCheckedChangeListener { buttonView, isChecked ->
            audioManager.isSpeakerphoneOn = isChecked
            mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let {
                Client.global.socket.send(
                    GatewayOp.VOICE,
                    Gson().toJsonTree(
                        VoiceConnectSend(
                            it.id,
                            isChecked,
                            audioManager.isMicrophoneMute
                        )
                    )
                )
            }
        }

        button7.setOnCheckedChangeListener { buttonView, isChecked ->
            mChatSharedViewModel.selectedCurrentVoiceChannel.value = null
            mChatSharedViewModel.switchingChannelVoiceLD.value = false
            dismiss()
        }
        mChatSharedViewModel.selectedCurrentVoiceChannel.observe(viewLifecycleOwner, Observer {
            it?.let {
                voice_channel_id.text = "#${it.name}"
                voice_guild_name_tv.text =
                    "${it.guild?.name}"
            }
        })

        mChatSharedViewModel.voiceStateUpdateLD.observe(viewLifecycleOwner, Observer {
            mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let { channel ->
                if (!it.channelId.isNullOrEmpty() && !it.guildId.isNullOrEmpty() && !it.sessionId.isNullOrEmpty() && channel.id == it.channelId) {//add
                    mVoiceUserList.removeIf { user ->
                        user.id == it.userId
                    }
                    Client.global.users[it.userId]?.let { user ->
                        user.isSelfDeaf = it.isSelfDeaf
                        user.isSelfMute = it.isSelfMute
                        mVoiceUserList.add(user)
                        mAdapter.notifyDataSetChanged()
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

        mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let {
            voice_channel_id.text = "#${it.name}"
        }

        mChatSharedViewModel.selectedCurrentVoiceChannel.value?.let {
            voice_guild_name_tv.text = it.guild?.name
        }

        mChatSharedViewModel.voiceSpeakerOnLD.observe(viewLifecycleOwner, Observer {
            button6.isChecked = it
        })

        mChatSharedViewModel.voiceLeaveLD.observe(viewLifecycleOwner, Observer {
            voice_channel_id.text = "语音已断开"
        })

        mChatSharedViewModel.selectedCurrentVoiceChannel.observe(viewLifecycleOwner, Observer {
            it?.let {
                mVoiceUserList.clear()
                (it as VoiceChannel).voiceStates.forEach {
                    Client.global.users[it.userId]?.let { user ->
                        user.isSelfMute = it.isSelfMute
                        user.isSelfDeaf = it.isSelfDeaf
                        mVoiceUserList.add(user)
                    }
                }
                mAdapter.notifyDataSetChanged()
            }
        })

        mChatSharedViewModel.voiceSpeakLD.observe(viewLifecycleOwner, Observer { speaking ->

            if (speaking.userId.isNotEmpty()) {
                mVoiceUserList.forEach {
                    it.isSpeaking =
                        (it.id == speaking.userId && speaking.isSpeaking == 1) || speaking.userId == Client.global.me.id
                }
                mAdapter.notifyDataSetChanged()
            }
        })

        voice_avatar_rr.layoutManager = LinearLayoutManager(requireContext())
        voice_avatar_rr.adapter = mAdapter


    }
}