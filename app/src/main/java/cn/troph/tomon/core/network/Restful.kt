package cn.troph.tomon.core.network

import cn.troph.tomon.core.network.services.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import hu.akarnokd.rxjava3.retrofit.*

class Restful {

    private val retrofit = Retrofit
        .Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(Configs.baseUrl).build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
    val guildService: GuildService = retrofit.create(GuildService::class.java)
    val channelService: ChannelService = retrofit.create(ChannelService::class.java)
    val messageService: MessageService = retrofit.create(MessageService::class.java)
    val roleService: RoleService = retrofit.create(RoleService::class.java)
    val guildMemberService: GuildMemberService = retrofit.create(GuildMemberService::class.java)
    val guildEmojiService: GuildEmojiService = retrofit.create(GuildEmojiService::class.java)
    val inviteService: InviteService = retrofit.create(InviteService::class.java)
}
