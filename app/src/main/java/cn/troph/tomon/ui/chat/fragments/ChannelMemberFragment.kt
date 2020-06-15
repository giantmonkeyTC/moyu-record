package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.DmChannel
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.chat.members.MemberListAdapter
import cn.troph.tomon.ui.chat.viewmodel.MemberViewModel
import cn.troph.tomon.ui.states.AppState
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_channel_member.*

class ChannelMemberFragment : Fragment() {
    private val mMemberVM: MemberViewModel by viewModels()
    private lateinit var mAdapter: MemberListAdapter

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = Client.global.channels[value]
                if (channel is TextChannelBase && channel !is DmChannel) {
                    mMemberVM.loadMemberList(value)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
            }
        return inflater.inflate(R.layout.fragment_channel_member, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMemberVM.getMembersLiveData().observe(viewLifecycleOwner, Observer {
            it?.let {
                mAdapter = MemberListAdapter(it)
                view_members.layoutManager = LinearLayoutManager(view.context)
                view_members.adapter = mAdapter
                if (view_members.itemDecorationCount > 0) {
                    view_members.removeItemDecorationAt(0)
                }
                view_members.addItemDecoration(StickyRecyclerHeadersDecoration(mAdapter))
            }
        })
    }

}