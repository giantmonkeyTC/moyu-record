package cn.troph.tomon.ui.states

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.network.socket.SocketClientState
import com.androidadvance.topsnackbar.TSnackbar
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import java.lang.NullPointerException
import java.time.LocalDateTime

class NetworkChangeReceiver() : BroadcastReceiver() {
    lateinit var mView: View
    lateinit var old: TSnackbar
    lateinit var new: TSnackbar

    override fun onReceive(context: Context?, intent: Intent?) {
        new = TSnackbar.make(
            mView,
            "网络连接中断",
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
                Toast.makeText(context, "网络连接中断", Toast.LENGTH_SHORT).show()
                Client.global.socket.close()
                if (old.isShown)
                    old.dismiss()
                new.show()
                old = new
            } else {
                Toast.makeText(context, "网络连接恢复", Toast.LENGTH_SHORT).show()
                Client.global.socket.close(1006, "network not available")
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
            "网络连接中断",
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