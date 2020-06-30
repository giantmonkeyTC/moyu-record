package cn.troph.tomon.ui.states

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.troph.tomon.core.Client
import com.androidadvance.topsnackbar.TSnackbar
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import java.lang.NullPointerException

class NetworkChangeReceiver() : BroadcastReceiver() {
    lateinit var mView: View
    lateinit var old: TSnackbar
    lateinit var new: TSnackbar

    override fun onReceive(context: Context?, intent: Intent?) {
        new = TSnackbar.make(
            mView,
            "Ground Control to Major Tom",
            TSnackbar.LENGTH_INDEFINITE
        ).apply {
            val view = this.view
            view.setBackgroundColor(Color.parseColor("#FA8072"))
            val text: TextView =
                view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
            text.setTextColor(Color.WHITE)
        }
        try {
            if (!isNetworkAvailable(context)) {
                Client.global.socket.close()
                if (old.isShown)
                    old.dismiss()
                new.show()
                old = new
            } else {
                Client.global.socket.open()
                old.dismiss()
            }
        } catch (
            e: NullPointerException
        ) {
            e.printStackTrace()
        }
    }

    fun setTopView(view: View) {
        this.mView = view
        old = TSnackbar.make(
            mView,
            "Ground Control to Major Tom",
            TSnackbar.LENGTH_INDEFINITE
        ).apply {
            val view = this.view
            view.setBackgroundColor(Color.parseColor("#FA8072"))
            val text: TextView =
                view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
            text.setTextColor(Color.WHITE)
        }
    }

    private fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: android.net.NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }
}