package cn.troph.tomon.core.network

import cn.troph.tomon.BuildConfig
import cn.troph.tomon.core.network.services.*
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Restful {

    private val client: OkHttpClient by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.BUILD_TYPE.equals("debug")) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        OkHttpClient.Builder().addInterceptor(interceptor).callTimeout(5, TimeUnit.MINUTES).build()
    }

    private val retrofit = Retrofit
        .Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(Configs.baseUrl)
        .client(client)
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
    val guildService: GuildService = retrofit.create(GuildService::class.java)
    val channelService: ChannelService = retrofit.create(ChannelService::class.java)
    val messageService: MessageService = retrofit.create(MessageService::class.java)
    val roleService: RoleService = retrofit.create(RoleService::class.java)
    val guildMemberService: GuildMemberService = retrofit.create(GuildMemberService::class.java)
    val guildEmojiService: GuildEmojiService = retrofit.create(GuildEmojiService::class.java)
    val inviteService: InviteService = retrofit.create(InviteService::class.java)
}
