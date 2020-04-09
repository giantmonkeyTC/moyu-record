package cn.troph.tomon.core.collections

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData
import cn.troph.tomon.core.structures.Guild
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class GuildCollection(client: Client, m: Map<String, Guild>? = null) :
    BaseCollection<Guild>(client, m) {

    override fun instantiate(data: JsonData): Guild? {
        return Guild(client, data);
    }

    fun fetch(sync: Boolean = true): Observable<List<Guild>?> {
        return client.rest.guildService.getGuilds(client.token).subscribeOn(Schedulers.io()).map {
            return@map client.actions.guildFetch(it)
        }
    }

}