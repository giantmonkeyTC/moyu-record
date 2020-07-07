package cn.troph.tomon.core.utils

import android.content.Context
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

object FileUtils {
    fun sizeConverter(size: String): String {
        val float = size.toFloat()
        return if (float < 1000f) "${String.format("%.2f", float)} B"
        else if (float > 1000f && float < 1000000f) "${String.format("%.2f", float / 1000f)} KB"
        else "${String.format("%.2f", float / 1000000f)} MB"
    }

    fun loadJSONFromAsset(context: Context, fileName: String): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = context.getAssets().open(fileName)
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}