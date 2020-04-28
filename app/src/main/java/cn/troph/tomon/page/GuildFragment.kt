package cn.troph.tomon.page


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.utils.Url
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.bottom_sheet_guild.view.*
import kotlinx.android.synthetic.main.fragment_guild.*
import java.util.*

class GuildFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_guild, container, false)
        view.setOnTouchListener(View.OnTouchListener(
            function = fun(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    Toast.makeText(context, "action down", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        ))
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = GuildListAdapter()
            visibility = View.VISIBLE
        }
        guildFab.setOnClickListener { callBottomSheet() }
    }

    private fun callBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_guild, null)
        val dialog = BottomSheetDialog(this.requireContext())
        dialog.setContentView(view)
        view.cancel_button.setOnClickListener {
            dialog.dismiss()
        }
        val rest = Restful()
        view.join_guild_button.setOnClickListener {
            Client.global.guilds.join(Url.parseInviteCode("https://beta.tomon.co/invite/FQmCup"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { guild ->
                        if (guild == null)
                            println("joined guild") else
                            println(guild.name)
                    }, { error -> println(error) }, { println("done") }
                )
        }
        dialog.show()
    }


}
