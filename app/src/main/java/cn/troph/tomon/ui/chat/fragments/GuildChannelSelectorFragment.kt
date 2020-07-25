package cn.troph.tomon.ui.chat.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.Voice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.GuildChannel
import cn.troph.tomon.ui.chat.messages.OnItemClickListener
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import cn.troph.tomon.ui.states.AppState
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.orhanobut.logger.Logger
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_guild_channel_selector.*

class GuildChannelSelectorFragment : Fragment() {
    private val mChatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private var disposable: Disposable? = null
    private var mRtcEngine: RtcEngine? = null
    private var mGuildVoiceChannel: GuildChannel? = null
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
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { guildId = it.guildId }
        val guildChannelAdapter = GuildChannelSelectorAdapter().apply { hasStableIds() }
        view_guild_channels.layoutManager = LinearLayoutManager(requireContext())
        view_guild_channels.adapter = guildChannelAdapter
        guildChannelAdapter.onItemClickListner = object : OnVoiceChannelClick {
            override fun onVoiceChannelSelected(channel: GuildChannel) {
                //check permission of microphone first
                Dexter.withContext(requireContext())
                    .withPermission(Manifest.permission.RECORD_AUDIO)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                            initAgoraEngineAndJoinChannel()
                            val voiceBottomSheet = VoiceBottomSheet()
                            voiceBottomSheet.show(parentFragmentManager, null)
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: PermissionRequest?,
                            p1: PermissionToken?
                        ) {

                        }

                        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                        }
                    }).check()
            }
        }

        //setup audio call
        mChatSharedViewModel.voiceMicControllerLD.observe(viewLifecycleOwner, Observer {
            mRtcEngine?.let { engine ->
                engine.enableLocalAudio(it)
                engine.muteLocalAudioStream(it)
            }
        })
        mChatSharedViewModel.voiceSoundControllerLD.observe(viewLifecycleOwner, Observer {
            mRtcEngine?.let { engine ->
                engine.muteAllRemoteAudioStreams(it)
            }
        })
        mChatSharedViewModel.voiceEarPhoneControllerLD.observe(viewLifecycleOwner, Observer {
            mRtcEngine?.let { engine ->
                engine.setEnableSpeakerphone(it)
            }
        })
        mChatSharedViewModel.voiceLeaveControllerLD.observe(viewLifecycleOwner, Observer {
            mRtcEngine?.let { engine ->
                engine.leaveChannel()
            }
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


    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        joinChannel()
    }

    private fun joinChannel() {
        val accessToken =
            "0061f43061ebe7243348efad474298c2bcbIAD68/8ynnO5R864kv/XyFKbvMCxqgn0O0RdYzdECZQ1Ywx+f9gAAAAAEAC+3ac7qCgdXwEAAQCoKB1f"
        // Allows a user to join a channel.
        mRtcEngine?.joinChannel(
            accessToken,
            "test",
            "",
            0
        ) // if you do not specify the uid, we will generate the uid for you
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(
                requireContext(),
                getString(R.string.agora_app_id),
                object : IRtcEngineEventHandler() {

                    override fun onJoinChannelSuccess(p0: String?, p1: Int, p2: Int) {
                        super.onJoinChannelSuccess(p0, p1, p2)
                        Logger.d("Joined Success:${p1}")
                        mGuildVoiceChannel?.let {
                            mChatSharedViewModel.voiceGuildVoiceStatusLD.value = it
                        }

                    }

                    override fun onLeaveChannel(p0: RtcStats?) {
                        super.onLeaveChannel(p0)
                        mChatSharedViewModel.voiceGuildVoiceLeaveLD.value = true
                    }

                    override fun onError(p0: Int) {
                        super.onError(p0)
                        Logger.d("Error Voice:${p0}")
                    }
                }
            )
            mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(true)

        } catch (e: Exception) {
            Logger.d(e.message)
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(
                    e
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

}