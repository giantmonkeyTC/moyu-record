package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.services.GuildMemberService
import cn.troph.tomon.core.structures.GuildMember
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.report_frag.*

class ReportFragment(private val target_id: String, private val type: Int) :
    BottomSheetDialogFragment() {

    private var mCheckId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return LayoutInflater.from(requireContext()).inflate(R.layout.report_frag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_report.setOnClickListener {
            if (mCheckId == 0) {
                if (input_report.text.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "请填写具体违规内容", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            Client.global.rest.guildMemberService.reportMember(
                GuildMemberService.ReportMember(
                    target_id,
                    mCheckId,
                    type,
                    input_report.text.toString().trim()
                )
            ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                Toast.makeText(requireContext(), R.string.report_success, Toast.LENGTH_SHORT).show()
                dismiss()
            }, {
                Toast.makeText(requireContext(), R.string.report_success, Toast.LENGTH_SHORT).show()
                dismiss()
            })
        }
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioButton -> {
                    mCheckId = 1
                }
                R.id.radioButton2 -> {
                    mCheckId = 2
                }
                R.id.radioButton3 -> {
                    mCheckId = 3
                }
                R.id.radioButton4 -> {
                    mCheckId = 4
                }
                R.id.radioButton5 -> {
                    mCheckId = 0
                }
            }
        }
    }
}