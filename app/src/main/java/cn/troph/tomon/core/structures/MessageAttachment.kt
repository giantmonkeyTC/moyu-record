package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class MessageAttachment(client: Client, data: JsonData) : Base(client, data) {
    var id: String = ""
    var filename: String = ""
    var url: String = ""
    var size: Int = 0
    var width: Int = 0
    var height: Int = 0

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.containsKey("id")) {
            id = data["id"] as String
        }
        if (data.containsKey("filename")) {
            filename = data["filename"] as String
        }
        if (data.containsKey("size")) {
            size = data["size"] as Int
        }
        if (data.containsKey("url")) {
            url = data["url"] as String
        }
        if (data.containsKey("width")) {
            width = data["width"] as Int
        }
        if (data.containsKey("height")) {
            height = data["height"] as Int
        }
    }

    override fun toString(): String {
        return "[CoreMessageAttachment $id] { filename: $filename }"
    }
}