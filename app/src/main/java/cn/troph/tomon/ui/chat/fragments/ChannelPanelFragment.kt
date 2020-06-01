package cn.troph.tomon.ui.chat.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.chat.messages.MessageListAdapter
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_channel_panel.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.FileOutputStream

class ChannelPanelFragment : Fragment() {
    companion object {
        private const val REQUEST_SYSTEM_CAMERA = 1
        private const val REQUEST_SYSTEM_ALBUM = 2
        private const val REQUEST_CAMERA_PERMISSION = 3
    }

    private var channelId: String? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed && value != null) {
                val channel = Client.global.channels[value]
                if (channel is TextChannelBase) {
                    channel.messages.fetch().observeOn(AndroidSchedulers.mainThread()).subscribe {
                    }
                }
            }
        }
    private var updateEnabled: Boolean = false
        set(value) {
            field = value
            if (field) {
                editText.setText(message?.content)
                bar_update_message.visibility = View.VISIBLE
            } else {
                editText.text = null
                bar_update_message.visibility = View.GONE
            }

        }
    private var message: Message? = null
    private var imageUri: Uri? = null
    private var imagePath: String? = null

    init {
        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                message = it.message
                updateEnabled = it.flag
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = view.findViewById<RecyclerView>(R.id.view_messages)
        val bar_update_message = view.findViewById<LinearLayout>(R.id.bar_update_message)
        val btn_send: ImageView = view.findViewById(R.id.btn_message_send)
        val edit_text: EditText = view.findViewById(R.id.editText)
        val btn_message_menu: ImageView = view.findViewById(R.id.btn_message_menu)
        val btn_update_text_cancel: TextView = view.findViewById(R.id.btn_update_message_cancel)
        btn_send.setOnClickListener {

            if (!AppState.global.updateEnabled.value.flag)
                (Client.global.channels[channelId
                    ?: ""] as TextChannel).messages.create(edit_text.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error -> println(error) }
                    .subscribe {
                        edit_text.text = null
                    }
            else
                message!!.update(edit_text.text.toString()).observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
                    println(
                        error
                    )
                }.subscribe {
                    AppState.global.updateEnabled.value =
                        UpdateEnabled(flag = false)
                    edit_text.text = null
                }
        }
        list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessageListAdapter().apply {
                hasStableIds()
            }
        }
        list.addOnScrollListener(MessageListOnScrollListener(list.adapter as MessageListAdapter))
        btn_update_text_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }
        btn_message_menu.setOnClickListener {
            val openAlbumIntent = Intent(Intent.ACTION_GET_CONTENT)
            openAlbumIntent.setType("image/*")
            startActivityForResult(openAlbumIntent, REQUEST_SYSTEM_ALBUM)
        }
        btn_message_menu.setOnLongClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED
            )
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
                )
            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePhotoIntent.resolveActivityInfo(
                    requireActivity().getPackageManager(),
                    PackageManager.MATCH_DEFAULT_ONLY
                ) != null
            ) {
                val imageFile: File? = storeImage()
                if (imageFile != null) {
                    imageUri = try {
                        FileProvider.getUriForFile(
                            requireContext(),
                            "cn.troph.tomon.fileprovider",
                            imageFile
                        )
                    } catch (e: IllegalArgumentException) {
                        Log.e(
                            "File Selector",
                            "The selected file can't be shared: $imageFile"
                        )
                        null
                    }
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(takePhotoIntent, REQUEST_SYSTEM_CAMERA)
                }

            } else {
                //TODO Unable to start system camera application
            }
            true
        }
    }

    private fun storeImage(): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${timeStamp}_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var imageFile: File? = null
        try {
            imageFile = File.createTempFile(fileName, ".jpg", storageDir)
            imagePath = imageFile.path
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_SYSTEM_CAMERA && resultCode == Activity.RESULT_OK) {
            MediaScannerConnection
                .scanFile(requireContext(), arrayOf(imageUri?.path), null, null)
            val file = File(imagePath)
            if (file != null) {
                val requestFile = file.asRequestBody()
                val requestBody =
                    "{\"content\":null,\"nonce\":\"1227791868142817280\"}".toRequestBody()
                val map = mutableMapOf<String, RequestBody>()
                map.put("payload_json", requestBody)
                val body = MultipartBody.Part.createFormData(file.name, file.name, requestFile)
                (Client.global.channels[channelId!!] as TextChannel).messages.uploadAttachments(
                    partMap = map,
                    files = body
                ).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    println(it.attachments.length)
                }
            }
        } else if (requestCode == REQUEST_SYSTEM_ALBUM && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri = data?.data!!
            val file = createTempFile(
                "123456",//TODO CHANGE TO SNOWFLAKE
                ".jpg",
                activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            val outputStream = FileOutputStream(file)
            try {
                outputStream.write(
                    IOUtils.readStreamAsBytesArray(
                        requireActivity().contentResolver.openInputStream(
                            imageUri
                        )
                    )
                )
            } catch (e: IOException) {
                println(e)
            }
            if (file != null) {
                val requestFile = file.asRequestBody()
                val requestBody =
                    "{\"content\":null,\"nonce\":\"1227791868142817280\"}".toRequestBody()
                val map = mutableMapOf<String, RequestBody>()
                map.put("payload_json", requestBody)
                val body = MultipartBody.Part.createFormData(file.name, file.name, requestFile)
                (Client.global.channels[channelId!!] as TextChannel).messages.uploadAttachments(
                    partMap = map,
                    files = body
                ).observeOn(AndroidSchedulers.mainThread()).subscribe {
                    file.delete()
                }
            }
        }
    }


}

class MessageListOnScrollListener(val adapter: MessageListAdapter) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == 2 && canScrollUp(recyclerView)) {

        }
    }

    /** return true if the list can't go up */
    fun canScrollUp(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(-1)
    }

    /** return true if the list can't go down */
    fun canScrollDown(recyclerView: RecyclerView): Boolean {
        return !recyclerView.canScrollVertically(1)
    }

}