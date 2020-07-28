package cn.troph.tomon.core.structures

import com.google.gson.annotations.SerializedName

//send when joining voice channel
data class VoiceConnect(
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("self_deaf") val self_deaf: Boolean,
    @SerializedName("self_mute") val selfMute: Boolean
)

//allowed to join voice channel
data class VoiceAllowConnect(
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("token") val tokenAgora: String,
    @SerializedName("voice_id") val voiceUserIdAgora: Int
)