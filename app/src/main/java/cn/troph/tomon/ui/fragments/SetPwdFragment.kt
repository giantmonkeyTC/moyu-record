package cn.troph.tomon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cn.troph.tomon.R
import cn.troph.tomon.ui.activities.OptionalLoginActivity
import kotlinx.android.synthetic.main.layout_set_pwd.*
import kotlinx.android.synthetic.main.layout_set_pwd.view.*

class SetPwdFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_set_pwd, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        set_pwd_next.setOnClickListener {
            getUserInfo().pwd = set_pwd.editText2.toString()
            activateAccount()
        }
    }

    private fun getUserInfo(): OptionalLoginActivity.UserInfo {
        return (requireActivity() as OptionalLoginActivity).userInfo
    }

    private fun activateAccount() {
        val fragment = ActivateAccountFragment()
        fragmentAdd(fragment)
    }

    private fun fragmentAdd(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in_right_custom,
                R.anim.no_animation
            )
            add(R.id.content, fragment)
            hide(getInstance())
            addToBackStack(null)
        }.commit()
    }

    private fun getInstance(): Fragment {
        return this
    }

}