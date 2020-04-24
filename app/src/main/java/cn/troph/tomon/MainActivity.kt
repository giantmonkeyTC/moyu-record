package cn.troph.tomon

import android.os.Bundle
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val adapter = ViewPagerAdapter(supportFragmentManager)
//        adapter.addFragment(GuildFragment())
//        adapter.addFragment(MessageFragment())
//        adapter.addFragment(MemberFragment())
//        viewPager.adapter = adapter
//        viewPager.currentItem = 1

        val client = Client.global
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
            unionId = "qiang.l.x@gmail.com",
            password = "1wq23re45ty67ui8"
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
