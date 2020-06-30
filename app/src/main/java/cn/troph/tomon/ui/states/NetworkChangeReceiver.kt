package cn.troph.tomon.ui.states

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.widget.TextView
import com.androidadvance.topsnackbar.TSnackbar
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import java.lang.NullPointerException

class NetworkChangeReceiver() : BroadcastReceiver() {
    lateinit var snackbar: TSnackbar
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (isNetworkAvailable(context)) {
                snackbar.dismiss()
            } else {
                snackbar.show()
            }
        } catch (
            e: NullPointerException
        ) {
            e.printStackTrace()
        }
    }

    fun setTopSnackbar(snackbar: TSnackbar) {
        this.snackbar = snackbar
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