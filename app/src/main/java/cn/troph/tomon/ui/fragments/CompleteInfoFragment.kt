package cn.troph.tomon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.troph.tomon.R

class CompleteInfoFragment:Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_set_info,null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}