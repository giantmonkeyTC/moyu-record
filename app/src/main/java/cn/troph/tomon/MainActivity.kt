package cn.troph.tomon

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cn.troph.tomon.page.ChannelFragment
import cn.troph.tomon.page.GuildFragment
import cn.troph.tomon.page.MemberFragment
import cn.troph.tomon.page.MessageFragment

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

private const val DEBUG_TAG = "Gestures"




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GuildFragment())
        adapter.addFragment(MessageFragment())
        adapter.addFragment(MemberFragment())
        viewPager.adapter = adapter
        viewPager.currentItem = 1



    }


    class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager){

        private  val fragmentList : MutableList<Fragment> = ArrayList()

        override fun getItem(position: Int): Fragment {
           return fragmentList[position];
        }

        override fun getCount(): Int {
           return fragmentList.size
        }

        fun addFragment(fragment: Fragment){
            fragmentList.add(fragment)
        }

    }


}
