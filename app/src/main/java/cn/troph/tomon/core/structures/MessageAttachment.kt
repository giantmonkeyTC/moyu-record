package cn.troph.tomon.core.structures

import cn.troph.tomon.core.Client
import cn.troph.tomon.core.JsonData

class MessageAttachment(client: Client, data: JsonData) : Base(client, data) {

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

    override fun patch(data: JsonData) {
        super.patch(data)
        if (data.contains("id")) {
            id = data["id"] as String
        }
        if (data.contains("filename")) {
            fileName = data["filename"] as String
        }
        if (data.contains("size")) {
            size = data["size"] as Int
        }
        if (data.contains("url")) {
            url = data["url"] as String
        }
        if (data.contains("width")) {
            width = data["width"] as Int
        }
        if (data.contains("height")) {
            height = data["height"] as Int
        }
    }

}