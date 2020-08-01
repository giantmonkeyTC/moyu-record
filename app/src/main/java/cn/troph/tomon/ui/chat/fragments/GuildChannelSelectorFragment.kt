package cn.troph.tomon.ui.chat.fragments

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
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
import cn.troph.tomon.core.events.GuildVoiceSelectorEvent
import cn.troph.tomon.core.events.VoiceSpeakEvent
import cn.troph.tomon.core.network.socket.GatewayOp
import cn.troph.tomon.core.structures.*
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.chat.viewmodel.notifyObserver
import cn.troph.tomon.ui.states.AppState
import com.github.nisrulz.sensey.ProximityDetector
import com.github.nisrulz.sensey.Sensey
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
    private lateinit var mWakeLock: PowerManager.WakeLock
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "tomon:xxx_new_wake_log_111_xxx"
        )
        Sensey.getInstance().init(requireContext())
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

                            VoiceBottomSheet().show(parentFragmentManager, null)
                            if (mChatSharedViewModel.selectedCurrentVoiceChannel.value == null) {
                                mChatSharedViewModel.switchingChannelVoiceLD.value = false
                                val audioManager =
                                    requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
                                if (mChatSharedViewModel.selectedCurrentVoiceChannel.value?.id != channel.id) {
                                    mChatSharedViewModel.voiceLeaveClick.value = true
                                    mChatSharedViewModel.switchingChannelVoiceLD.value = true
                                }else{
                                    mChatSharedViewModel.selectedCurrentVoiceChannel.notifyObserver()
                                }
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

        //接收socket leave
        mChatSharedViewModel.voiceSocketLeaveLD.observe(viewLifecycleOwner, Observer {
            //disconnect voice ws
            Client.global.voiceSocket.close()
        })

        //接收socket join
        mChatSharedViewModel.voiceSocketJoinLD.observe(viewLifecycleOwner, Observer {
            Client.global.voiceSocket.open()
            joinChannel(it.tokenAgora, it.voiceUserIdAgora, it.channelId!!)
        })

        //点击关闭voice panel
        mChatSharedViewModel.voiceLeaveClick.observe(viewLifecycleOwner, Observer {
            Client.global.socket.send(GatewayOp.VOICE, Gson().toJsonTree(VoiceLeaveConnect()))
        })

        //voice ws状态
        mChatSharedViewModel.voiceSocketStateLD.observe(viewLifecycleOwner, Observer {
            if (it) {//语音socket打开并鉴权
                Client.global.voiceSocket.send(
                    GatewayOp.DISPATCH, Gson().toJsonTree(
                        VoiceIdentify(
                            sessionId = Client.global.socket.getSesstion()!!,
                            voiceId = mChatSharedViewModel.voiceSocketJoinLD.value?.voiceUserIdAgora!!,
                            userId = Client.global.me.id
                        )
                    )
                )
            } else {//语音socket关闭
                mRtcEngine?.leaveChannel()
            }
        })
    }

    private val mProximityListener = object : ProximityDetector.ProximityListener {
        override fun onFar() {
            if (!mWakeLock.isHeld) {
                mWakeLock.acquire(3600 * 1000)
            }
            mChatSharedViewModel.voiceSpeakerOnLD.value = true
        }

        override fun onNear() {
            if (!mWakeLock.isHeld) {
                mWakeLock.release()
            }
            mChatSharedViewModel.voiceSpeakerOnLD.value = false
        }

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
                            p0?.forEach {
                                if (it.vad == 1) {
                                    if (it.volume > 30) {
                                        Logger.d("speaking")
                                        Client.global.voiceSocket.send(
                                            GatewayOp.SPEAK,
                                            Gson().toJsonTree(Speaking(1))
                                        )
                                        Client.global.eventBus.postEvent(
                                            VoiceSpeakEvent(
                                                Speaking(
                                                    1,
                                                    Client.global.me.id
                                                )
                                            )
                                        )
                                    } else {
                                        Logger.d("no speaking")
                                        Client.global.voiceSocket.send(
                                            GatewayOp.SPEAK,
                                            Gson().toJsonTree(Speaking(0))
                                        )
                                        Client.global.eventBus.postEvent(
                                            VoiceSpeakEvent(
                                                Speaking(
                                                    0,
                                                    Client.global.me.id
                                                )
                                            )
                                        )

                                    }
                                }
                            }
                        }

                        override fun onLeaveChannel(p0: RtcStats?) {
                            super.onLeaveChannel(p0)
                            mHandler.post {
                                mChatSharedViewModel.switchingChannelVoiceLD.value?.let {
                                    if (it) {
                                        val audioManager =
                                            requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                                        Client.global.socket.send(
                                            GatewayOp.VOICE,
                                            Gson().toJsonTree(
                                                VoiceConnectSend(
                                                    mSelectedVoiceChannel.id,
                                                    audioManager.isStreamMute(AudioManager.STREAM_MUSIC),
                                                    audioManager.isMicrophoneMute
                                                )
                                            )
                                        )
                                    }
                                }
                                mChatSharedViewModel.switchingChannelVoiceLD.value = false
                                mChatSharedViewModel.selectedCurrentVoiceChannel.value = null
                                Client.global.eventBus.postEvent(GuildVoiceSelectorEvent(""))
                                Sensey.getInstance().stopProximityDetection(mProximityListener)
                                Logger.d("Leave Success")
                            }
                        }

                        override fun onJoinChannelSuccess(p0: String?, p1: Int, p2: Int) {
                            super.onJoinChannelSuccess(p0, p1, p2)
                            mHandler.post {
                                Logger.d("Join Success")
                                mChatSharedViewModel.selectedCurrentVoiceChannel.value =
                                    mSelectedVoiceChannel
                                Client.global.eventBus.postEvent(
                                    GuildVoiceSelectorEvent(
                                        mSelectedVoiceChannel.id
                                    )
                                )
                            }
                            Sensey.getInstance().startProximityDetection(mProximityListener)
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
                mRtcEngine?.enableAudioVolumeIndication(200, 3, true)
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
        Sensey.getInstance().stop()
    }

}

interface SwitchChannelVoiceCallback {
    fun onSwitchFinish()
}