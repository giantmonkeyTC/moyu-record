package cn.troph.tomon.ui.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import cn.troph.tomon.R
import cn.troph.tomon.ui.chat.messages.BotCommandAdapter
import cn.troph.tomon.ui.chat.messages.OnItemClickListener
import cn.troph.tomon.ui.chat.viewmodel.ChatSharedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bot_command_fragment.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import net.andreinc.jasuggest.JaCacheConfig
import net.andreinc.jasuggest.JaSuggest


class BotCommandBottomSheet : BottomSheetDialogFragment() {
    private val mChatSharedViewModel: ChatSharedViewModel by activityViewModels()
    private val mBotCommandList = mutableListOf<String>()
    private val mBotSearchCommandList = mutableListOf<String>()
    private val mBotCommandAdapter =
        BotCommandAdapter(mBotSearchCommandList, object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                mChatSharedViewModel.botCommandSelectedLD.value = mBotSearchCommandList[position]
                dismiss()
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bot_command_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jaCacheConfig = JaCacheConfig.defaultConfig()
        mBotCommandList.add("123")
        mBotCommandList.add("You")
        mBotCommandList.add("help")
        mBotCommandList.add("test")
        mBotCommandList.add("bot")
        mBotCommandList.add("123123")
        mBotCommandList.add("123abc")
        mBotCommandList.add("qwer1")
        mBotCommandList.add("qwerty")

        val jaSuggest =
            JaSuggest.builder().ignoreCase().withCache(jaCacheConfig).buildFrom(mBotCommandList)
        bot_command_rr.layoutManager = LinearLayoutManager(requireContext())
        mBotSearchCommandList.addAll(mBotCommandList)
        bot_command_rr.adapter = mBotCommandAdapter
        OverScrollDecoratorHelper.setUpOverScroll(
            bot_command_rr,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )
        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    mBotSearchCommandList.clear()
                    mBotSearchCommandList.addAll(jaSuggest.findSuggestions(it))
                    mBotCommandAdapter.notifyDataSetChanged()
                }
                return true
            }
        })
    }
}