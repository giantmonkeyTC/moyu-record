package cn.troph.tomon.ui.chat.fragments

import android.app.ActivityOptions
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import cn.troph.tomon.R
import cn.troph.tomon.core.Client
import cn.troph.tomon.ui.activities.EntryOptionActivity
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.dialog_check_logout.*

class LogoutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.porofile_check_logout)
                .setPositiveButton(R.string.profile_confirm_logout,
                    DialogInterface.OnClickListener { dialog, id ->
                        Client.global.preferences.edit {
                            putString(LAST_CHANNEL_ID, null)
                        }
                        Client.global.me.logout()
                        gotoEntryOption()
                    })
                .setNegativeButton(R.string.profile_cancel_logout,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun gotoEntryOption() {
        val intent = Intent(requireContext(), EntryOptionActivity::class.java)
        startActivity(
            intent,
            ActivityOptions.makeCustomAnimation(
                requireContext(),
                R.animator.top_to_bottom_anim,
                R.animator.top_to_bottom_anim
            ).toBundle()
        )
        requireActivity().finish()
    }
}