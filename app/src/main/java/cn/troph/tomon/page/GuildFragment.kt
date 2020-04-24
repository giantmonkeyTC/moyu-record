package cn.troph.tomon.page


import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment

import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.Restful
import cn.troph.tomon.core.utils.Url
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.bottom_sheet_guild.view.*
import kotlinx.android.synthetic.main.fragment_guild.*

class GuildFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view :View = inflater.inflate(R.layout.fragment_guild, container, false)
        view.setOnTouchListener(View.OnTouchListener(
            function = fun(v:View, event: MotionEvent):Boolean{
                if(event.action == MotionEvent.ACTION_DOWN){
                    Toast.makeText(context,"action down",Toast.LENGTH_SHORT).show()
                }
               return true
            }
        ))
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        guildFab.setOnClickListener{callBottomSheet()}
    }
    private fun callBottomSheet(){
        val view = layoutInflater.inflate(R.layout.bottom_sheet_guild, null)
        val dialog = BottomSheetDialog(this.requireContext())
        dialog.setContentView(view)
        view.cancel_button.setOnClickListener {
            dialog.dismiss()
            println("12423462356")
        }
        val rest = Restful()
        view.join_guild_button.setOnClickListener {
            println("124346")
            println(Client.global.token == "")
            println(if (Client.global.token == "") Client.global.token else "there is no token!!!")
            rest.inviteService.join(
                Url.parseInviteCode("https://beta.tomon.co/invite/2AGdNF"),
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijc3ODIyNzQzNjQyMzc4MjQwIiwiaWF0IjoxNTc4MzAyNTA5fQ.ArVvMMFNLP3nxm60aZRWyjKBgu6tVdoe8oGEwqlaKdo"
            )
//            Client.global.guilds.join(Url.parseInviteCode("https://beta.tomon.co/invite/2AGdNF"))
        }
        dialog.show()
    }


}
