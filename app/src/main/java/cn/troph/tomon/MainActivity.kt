package cn.troph.tomon

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cn.troph.tomon.core.Client
import cn.troph.tomon.page.GuildFragment
import cn.troph.tomon.page.MemberFragment
import cn.troph.tomon.page.MessageFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.system.measureTimeMillis

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

        val client = Client()
        Single.create()
        Observable.just(client.me.username).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribeBy(onNext = {
                println("subscribe")
                println(it)
            })

        println(Thread.currentThread())
        GlobalScope.async {
            val time = measureTimeMillis {
                client.me.login(
                    emailOrPhone = "qiang.l.x@gmail.com",
                    password = "1wq23re45ty67ui8"
                )
            }
            println(Thread.currentThread())
            println("login finish $time")
        }
    }


    class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        private val fragmentList: MutableList<Fragment> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position];
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFragment(fragment: Fragment) {
            fragmentList.add(fragment)
        }

    }


}
