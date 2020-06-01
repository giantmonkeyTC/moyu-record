package cn.troph.tomon.ui.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import cn.troph.tomon.R

class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        val intent = intent
        val imageView = findViewById<ImageView>(R.id.preview_display)
        val text = findViewById<TextView>(R.id.text_test).apply {
            text = "123454"
        }
        val imageUri: Uri? = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT)!!
        println(imageUri)
    }

}