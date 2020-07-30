package cn.troph.tomon.ui.chat.fragments

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.events.VoiceSpeakEvent
import cn.troph.tomon.core.network.socket.GatewayOp
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.orhanobut.logger.Logger
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_guild_channel_selector.*

class GuildChannelSelectorFragment : Fragment() {
    private val mHandler = Handler()
    private val mChatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private var disposable: Disposable? = null
    private var mRtcEngine: RtcEngine? = null
    private lateinit var mSelectedVoiceChannel: GuildChannel
    var guildId: String? = null
        set(value) {
            field = value
            update()
            val guild = guildId?.let { Client.global.guilds[it] }
            disposable?.dispose()
            disposable = guild?.observable?.observeOn(AndroidSchedulers.mainThread())?.subscribe {
                update()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guild_channel_selector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAgoraEngine()
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { guildId = it.guildId }
        val guildChannelAdapter = GuildChannelSelectorAdapter().apply { hasStableIds() }
        view_guild_channels.layoutManager = LinearLayoutManager(requireContext())
        view_guild_channels.adapter = guildChannelAdapter
        guildChannelAdapter.onItemClickListner = object : OnVoiceChannelClick {
            override fun onVoiceChannelSelected(channel: GuildChannel) {
                mSelectedVoiceChannel = channel
                //check permission of microphone first
                Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.RECORD_AUDIO)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            val audioManager =
                                requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                            if (mChatSharedViewModel.selectedCurrentVoiceChannel.value == null) {
                                Client.global.socket.send(
                                    GatewayOp.VOICE,
                                    Gson().toJsonTree(
                                        VoiceConnectSend(
                                            channel.id,
                                            audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                                            audioManager.isMicrophoneMute
                                        )
                                    )
                                )
                            } else {
                                mChatSharedViewModel.selectedCurrentVoiceChannel.value = null
                                mHandler.postDelayed({
                                    Client.global.socket.send(
                                        GatewayOp.VOICE,
                                        Gson().toJsonTree(
                                            VoiceConnectSend(
                                                channel.id,
                                                audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                                                audioManager.isMicrophoneMute
                                            )
                                        )
                                    )
                                }, 1500)
                            }

                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                        ) {

                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            Toast.makeText(
                                requireContext(),
                                R.string.join_permission_msg,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }).check()
            }
        }

        mChatSharedViewModel.voiceLeaveChannelLD.observe(viewLifecycleOwner, Observer {
            //disconnect voice ws
            Client.global.voiceSocket.close()
            mRtcEngine?.leaveChannel()
        })

        mChatSharedViewModel.selectedCurrentVoiceChannel.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                Client.global.socket.send(GatewayOp.VOICE, Gson().toJsonTree(VoiceLeaveConnect()))
            } else {
                //switch channel
            }
        })
        mChatSharedViewModel.voiceSocketStateLD.observe(viewLifecycleOwner, Observer {
            if (it) {
                val voice = VoiceIdentify(
                    sessionId = Client.global.socket.getSesstion()!!,
                    voiceId = mChatSharedViewModel.voiceAllowConnectLD.value?.voiceUserIdAgora!!,
                    userId = Client.global.me.id
                )
                Client.global.voiceSocket.send(GatewayOp.DISPATCH, Gson().toJsonTree(voice))
            } else {

            }
        })

        mChatSharedViewModel.voiceAllowConnectLD.observe(viewLifecycleOwner, Observer {
            VoiceBottomSheet().show(parentFragmentManager, null)
            joinChannel(it.tokenAgora, it.voiceUserIdAgora, it.channelId!!)
            Client.global.voiceSocket.open()
        })


    }

    fun update() {
        if (guildId != "@me") {
            val guild = guildId?.let { Client.global.guilds[it] }
            val headerText = view?.findViewById<TextView>(R.id.text_channel_header_text)
            headerText?.text = guild?.name
            headerText?.setOnLongClickListener {
                ReportFragment(guild?.id!!, 2).show(requireActivity().supportFragmentManager, null)
                true
            }
        }
    }

    private fun joinChannel(token: String, uid: Int, channelId: String) {
        // online token
        mRtcEngine?.joinChannel(
            token,
            channelId,
            "Extra Optional Data", uid
        ) // if you do not specify the uid, we will generate the uid for you
    }

    private fun initializeAgoraEngine() {
        try {
            if (mRtcEngine == null) {
                mRtcEngine = RtcEngine.create(
                    requireContext(),
                    getString(R.string.agora_app_id),
                    object : IRtcEngineEventHandler() {

                        override fun onAudioVolumeIndication(
                            p0: Array<out AudioVolumeInfo>?,
                            p1: Int
                        ) {
                            super.onAudioVolumeIndication(p0, p1)
                            val myAudioInfo = p0?.find {
                                it.vad == 1
                            }
                            myAudioInfo?.let {
                                if (it.volume > 20) {
                                    Client.global.voiceSocket.send(
                                        GatewayOp.SPEAK,
                                        Gson().toJsonTree(Speaking(true))
                                    )
                                    mChatSharedViewModel.voiceSpeakLD.value =
                                        Speaking(true, Client.global.me.id)
                                } else {
                                    Client.global.voiceSocket.send(
                                        GatewayOp.SPEAK,
                                        Gson().toJsonTree(Speaking(false))
                                    )
                                    mChatSharedViewModel.voiceSpeakLD.value =
                                        Speaking(false, Client.global.me.id)
                                }
                            }
                        }

                        override fun onLeaveChannel(p0: RtcStats?) {
                            super.onLeaveChannel(p0)
                            Logger.d("Leave Channel")
                            mHandler.post {
                                Toast.makeText(requireContext(), "离开频道成功", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                        override fun onJoinChannelSuccess(p0: String?, p1: Int, p2: Int) {
                            super.onJoinChannelSuccess(p0, p1, p2)
                            mHandler.post {
                                Logger.d("Join Success")
                                Toast.makeText(requireContext(), "加入频道成功", Toast.LENGTH_SHORT)
                                    .show()
                                mChatSharedViewModel.selectedCurrentVoiceChannel.value =
                                    mSelectedVoiceChannel
                            }
                        }

                        override fun onError(p0: Int) {
                            super.onError(p0)
                            Logger.d("Error Voice:${p0}")
                            if (p0 == Constants.ERR_JOIN_CHANNEL_REJECTED) {
                                mHandler.post {
                                    Toast.makeText(
                                        requireContext(),
                                        "请先退出之前的语音频道再加入",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }

                        }
                    }
                )
                mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)
                mRtcEngine?.enableAudioVolumeIndication(250, 10, true)
            }
        } catch (e: Exception) {
            Logger.d(e.message)
            Toast.makeText(
                requireContext(),
                R.string.join_voice_fail,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

}