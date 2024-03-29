package cn.troph.tomon.core.collections

import android.util.Log
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Guild
import cn.troph.tomon.core.utils.SortedList
import cn.troph.tomon.ui.chat.fragments.Invite
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class GuildCollection(client: Client) :
    BaseCollection<Guild>(client) {

    private val sortedList: SortedList<Guild> =
        SortedList(
            Comparator { o1, o2 ->
                o1.compareTo(o2)
            }
        )

    override fun add(data: JsonObject, identify: CollectionIdentify?): Guild? {
        val ins = super.add(data, identify)
        if (ins != null) {
            sortedList.addIfNotExist(ins)
        }
        return ins
    }

    override fun remove(key: String): Guild? {
        val ins = super.remove(key)
        if (ins != null) {
            sortedList.remove(ins)
        }
        return ins
    }

    override fun instantiate(data: JsonObject): Guild? {
        return Guild(client, data)
    }

    fun fetch(sync: Boolean = true): Observable<List<Guild>?> {
        return client.rest.guildService.getGuilds(client.auth).subscribeOn(Schedulers.io()).map {
            return@map client.actions.guildFetch(it)
        }
    }

    val list get() = sortedList

    fun join(code: String): Observable<Guild?> {
        return client.rest.inviteService.join(code, client.auth).subscribeOn(Schedulers.io()).map {
            Logger.d(it)
            Guild(client, it)
        }
    }

    fun fetchInvite(code: String): Observable<Invite> {
        return client.rest.inviteService.fetch(code, client.auth).subscribeOn(Schedulers.io())
    }

}