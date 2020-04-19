package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
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
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    init {
        patchSelf(data)
    }

    private fun patchSelf(data: JsonObject) {
        if (data.has("id")) {
            id = data["id"].asString
        }
        if (data.has("filename")) {
            fileName = data["filename"] as String
        }
        if (data.has("size")) {
            size = data["size"] as Int
        }
        if (data.has("url")) {
            url = data["url"] as String
        }
        if (data.has("width")) {
            width = data["width"] as Int
        }
        if (data.has("height")) {
            height = data["height"] as Int
        }
    }

    override fun patch(data: JsonObject) {
        super.patch(data)
        patchSelf(data)
    }

}