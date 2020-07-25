package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.rtc.RtcEngine
import kotlinx.android.synthetic.main.voice_bottom_sheet.*

class VoiceBottomSheet(private val mRtcEngine: RtcEngine) : BottomSheetDialogFragment() {
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

        mChatSharedViewModel.voiceMicState.observe(viewLifecycleOwner, Observer {
            if (button4.isChecked != it)
                button4.isChecked = it

            button4.setOnCheckedChangeListener { buttonView, isChecked ->
                mChatSharedViewModel.voiceMicControllerLD.value = isChecked
            }
            button5.setOnCheckedChangeListener { buttonView, isChecked ->
                mChatSharedViewModel.voiceSoundControllerLD.value = isChecked
            }
            button6.setOnCheckedChangeListener { buttonView, isChecked ->
                mChatSharedViewModel.voiceEarPhoneControllerLD.value = isChecked
            }
            button7.setOnCheckedChangeListener { buttonView, isChecked ->
                mChatSharedViewModel.voiceLeaveControllerLD.value = isChecked
            }
        })
        mChatSharedViewModel.voiceSoundState.observe(viewLifecycleOwner, Observer {
            if (button5.isChecked != it)
                button5.isChecked = it
        })
        mChatSharedViewModel.voiceSpeakerState.observe(viewLifecycleOwner, Observer {
            if (button6.isChecked)
                button6.isChecked = it
        })


    }
}