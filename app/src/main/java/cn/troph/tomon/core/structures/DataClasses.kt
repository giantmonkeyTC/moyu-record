package cn.troph.tomon.core.structures

import com.google.gson.annotations.SerializedName

//send when joining voice channel
data class VoiceConnectSend(
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("self_deaf") val self_deaf: Boolean,
    @SerializedName("self_mute") val selfMute: Boolean
)

//allowed to join voice channel
data class VoiceAllowConnectReceive(
    @SerializedName("channel_id") val channelId: String?,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("token") val tokenAgora: String,
    @SerializedName("voice_id") val voiceUserIdAgora: Int
)

data class VoiceLeaveConnect(
    @SerializedName("channel_id") val channelId: String? = null,
    @SerializedName("self_deaf") val self_deaf: Boolean = false,
    @SerializedName("self_mute") val selfMute: Boolean = false
)

data class VoiceIdentify(
    @SerializedName("op") val op: Int = 0,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("voice_id") val voiceId: Int
)

data class Speaking(@SerializedName("speaking") val isSpeaking: Boolean)

data class VoiceUpdate(
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("guild_id") val guildId: String,
    @SerializedName("self_deaf") val self_deaf: Boolean = false,
    @SerializedName("self_mute") val selfMute: Boolean = false,
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("voice_id") val voiceId: Int
)

