package cn.troph.tomon.ui.chat.fragments

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.voice_bottom_sheet.*

class VoiceBottomSheet : BottomSheetDialogFragment() {

    private val mChatSharedViewModel: ChatSharedViewModel by activityViewModels()

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
        }

        button6.setOnCheckedChangeListener { buttonView, isChecked ->
            audioManager.isSpeakerphoneOn = isChecked
        }

        button7.setOnCheckedChangeListener { buttonView, isChecked ->
            mChatSharedViewModel.selectedCurrentVoiceChannel.value = null
            dismiss()
        }
        mChatSharedViewModel.selectedCurrentVoiceChannel.observe(viewLifecycleOwner, Observer {
            it?.let {
                voice_channel_id.text = "#${it.name}"
                voice_guild_name_tv.text =
                    "${it.guild?.name}"
            }
        })
        voice_channel_id.text = "#${mChatSharedViewModel.selectedCurrentVoiceChannel.value?.name}"
        voice_guild_name_tv.text =
            "${mChatSharedViewModel.selectedCurrentVoiceChannel.value?.guild?.name}"
    }
}