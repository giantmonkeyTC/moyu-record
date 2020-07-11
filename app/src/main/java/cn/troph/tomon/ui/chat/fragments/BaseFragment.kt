package cn.troph.tomon.ui.chat.fragments

import androidx.fragment.app.Fragment
import cn.troph.tomon.TomonApplication
import com.google.android.gms.analytics.Tracker

open class BaseFragment : Fragment() {

    fun getTracker(): Tracker {
        return (requireActivity().application as TomonApplication).getDefaultTracker()
    }

}