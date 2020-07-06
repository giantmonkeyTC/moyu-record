package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.utils.optInt
import com.google.gson.JsonObject

class MessageAttachment(client: Client, data: JsonObject) : Base(client, data) {

    var id: String = ""
        private set
    var fileName: String = ""
        private set
    var url: String = ""
        private set
    var size: Int = 0
        private set
    var width: Int? = null
        private set
    var height: Int? = null
        private set

    var type = ""

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("filename")) {
            fileName = data["filename"].asString
        }
        if (data.has("size")) {
            size = data["size"].asInt
        }
        if (data.has("url")) {
            url = data["url"].asString
        }
        if (data.has("width")) {
            width = data["width"].optInt
        }
        if (data.has("height")) {
            height = data["height"].optInt
        }
        if (data.has("type")) {
            type = data["type"].asString
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

}