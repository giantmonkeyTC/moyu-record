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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.core.structures.Message
import cn.troph.tomon.core.structures.TextChannel
import cn.troph.tomon.core.structures.TextChannelBase
import cn.troph.tomon.ui.chat.messages.MessageAdapter
import cn.troph.tomon.ui.chat.messages.MessageListAdapter
import cn.troph.tomon.ui.chat.messages.MessageViewModel
import cn.troph.tomon.ui.chat.messages.notifyObserver
import cn.troph.tomon.ui.states.AppState
import cn.troph.tomon.ui.states.UpdateEnabled
import com.alibaba.sdk.android.oss.common.utils.IOUtils
import com.orhanobut.logger.Logger
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

    private val msgViewModel: MessageViewModel by viewModels()

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
                    msgViewModel.loadTextChannelMessage(value)
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
    private lateinit var msgListAdapter: MessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AppState.global.updateEnabled.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Logger.d(it.message?.content)
                message = it.message
                updateEnabled = it.flag
            }
        AppState.global.channelSelection.observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                channelId = it.channelId
            }
        return inflater.inflate(R.layout.fragment_channel_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_message_send.setOnClickListener {

            if (!AppState.global.updateEnabled.value.flag)
                (Client.global.channels[channelId
                    ?: ""] as TextChannel).messages.create(editText.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error -> println(error) }
                    .subscribe {
                        msgViewModel.getMessageLiveData().value?.add(it)
                        msgViewModel.getMessageLiveData().notifyObserver()
                        editText.text = null
                    }
            else
                message!!.update(editText.text.toString())
                    .observeOn(AndroidSchedulers.mainThread()).doOnError { error ->
                        println(
                            error
                        )
                    }.subscribe {
                        AppState.global.updateEnabled.value =
                            UpdateEnabled(flag = false)
                        editText.text = null
                    }
        }
        msgViewModel.getMessageLiveData().observe(viewLifecycleOwner,
            Observer<MutableList<Message>?> {
                it?.let {
                    msgListAdapter = MessageAdapter(it)
                    view_messages.layoutManager = LinearLayoutManager(view.context)
                    view_messages.adapter = msgListAdapter
                    view_messages.scrollToPosition(msgListAdapter.itemCount - 1)
                }
            })
        view_messages.addOnScrollListener(MessageListOnScrollListener())
        btn_update_message_cancel.setOnClickListener {
            AppState.global.updateEnabled.value =
                UpdateEnabled(flag = false)
        }
        btn_message_menu.setOnClickListener {
            val openAlbumIntent = Intent(Intent.ACTION_GET_CONTENT)
            openAlbumIntent.type = "image/*"
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
                    requireActivity().packageManager,
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
                map["payload_json"] = requestBody
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

class MessageListOnScrollListener :
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