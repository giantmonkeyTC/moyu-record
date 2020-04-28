package cn.troph.tomon

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.SortedList
import cn.troph.tomon.page.GuildFragment
import cn.troph.tomon.page.MemberFragment
import cn.troph.tomon.page.MessageFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import kotlinx.android.synthetic.main.content_main.*
import java.io.IOException
import java.net.SocketException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(GuildFragment())
        adapter.addFragment(MessageFragment())
        adapter.addFragment(MemberFragment())
        viewPager.adapter = adapter
        RxJavaPlugins.setErrorHandler{e ->
            if (e is UndeliverableException) {
                println(e)
                return@setErrorHandler
            }
            if ((e is IOException) || (e is SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (e is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler;
            }
            Log.println(Log.ERROR,"NOPE","Undeliverable exception received, not sure what to do")
        }
        viewPager.currentItem = 1

        val client = Client.global
        client.me.clear()
        Observable.create(client.users).subscribeBy(
            onNext = { event ->
                println("user update")
                println(event)
            }
        )
        Observable.create(client.me).subscribeBy(
            onNext = {
                println("me update")
                println(client.me.username)
            },
            onError = { it.printStackTrace() },
            onComplete = { println("complete") }
        )
        Observable.create(client.guilds).subscribeBy(
            onNext = {
                println("${it.type}${it.obj?.icon}")
            },
            onError = { it.printStackTrace() }
        )
        client.login(
            emailOrPhone = "18516901224",
            password = "12345678"
        ).observeOn(AndroidSchedulers.mainThread())
            .subscribe({ user -> println(user) }, { error -> println(error) })

        val list = SortedList<String>()
        list.add("a")
        list.add("z")
        list.add("b")
        list.add("0")

//        Observable.timer(5, TimeUnit.SECONDS).flatMap {
//            return@flatMap Observable.create<String> { emitter ->
//                println("666")
//                client.me.update(mapOf("username" to "abcdefg"))
//                emitter.onNext("666")
//            }
//        }.observeOn(Schedulers.io()).subscribe {
//            println("trigger")
//        }
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
