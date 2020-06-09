package cn.troph.tomon.ui.chat.fragments

import android.app.Activity
import android.content.Context

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import cn.troph.tomon.R
import com.arthurivanets.bottomsheets.BaseBottomSheet
import com.arthurivanets.bottomsheets.config.BaseConfig
import com.arthurivanets.bottomsheets.config.Config

import kotlinx.android.synthetic.main.chat_bottom_sheet.view.*


class FileBottomSheetFragment(
    hostActivity: Activity,
    config: BaseConfig = Config.Builder(hostActivity).build(),
    private val onBottomSheetSelect: OnBottomSheetSelect
) : BaseBottomSheet(hostActivity, config) {

    override fun onCreateSheetContentView(context: Context): View {
        val view = LayoutInflater.from(context).inflate(R.layout.chat_bottom_sheet, this, false)
        view.button.setOnClickListener {
            onBottomSheetSelect.onItemSelected(0)
        }
        view.button2.setOnClickListener {
            onBottomSheetSelect.onItemSelected(1)
        }

        view.button3.setOnClickListener {
            onBottomSheetSelect.onItemSelected(2)
        }
        return view
    }

}

interface OnBottomSheetSelect {
    fun onItemSelected(index: Int)
}

