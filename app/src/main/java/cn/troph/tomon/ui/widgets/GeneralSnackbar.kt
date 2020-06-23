package cn.troph.tomon.ui.widgets

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import cn.troph.tomon.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import kotlinx.android.synthetic.main.snackbar_general.view.*


/** Usage: GeneralSnackbar
 *          .make(GeneralSnackbar.findSuitableParent(view),
 *                text,
 *                duration)
 *          .apply{ ...
 *                  show()}
 **/
class GeneralSnackbar(
    parent: ViewGroup,
    val content: View,
    contentViewCallback: MContentViewCallback
) :
    BaseTransientBottomBar<GeneralSnackbar>(parent, content, contentViewCallback) {

    companion object {
        fun make(
            parent: ViewGroup,
            text: String,
            duration: Int
        ): GeneralSnackbar {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.snackbar_general, parent, false)
            view.snackbar_text.text = text
            view.snackbar_action.visibility = View.GONE
            val callback = MContentViewCallback(view)
            val generalSnackbar = GeneralSnackbar(parent, view, callback)
            generalSnackbar.view.background = ColorDrawable(Color.TRANSPARENT)
            generalSnackbar.duration = duration
            return generalSnackbar
        }

        /** Get the appropriate parent view of the view parameter */
        fun findSuitableParent(view: View): ViewGroup? {
            var view: View? = view
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) { // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    fallback =
                        if (view.getId() == android.R.id.content) { // If we've hit the decor content view, then we didn't find a CoL in the
                            // hierarchy, so use it.
                            return view
                        } else { // It's not the content view but we'll use it as our fallback
                            view
                        }
                }
                if (view != null) { // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)
            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    fun setAction(
        actionText: String?,
        actionColor: Int? = null,
        action: () -> Unit
    ): GeneralSnackbar {
        this.content.snackbar_action.visibility = View.VISIBLE
        this.content.snackbar_action.text = actionText
        this.content.snackbar_action.setOnClickListener { action() }
        if (actionColor != null)
            this.content.snackbar_action.setTextColor(actionColor)
        return this
    }

    fun setTextColor(color: Int): GeneralSnackbar {
        this.content.snackbar_text.setTextColor(color)
        return this
    }


    class MContentViewCallback(private val view: View) :
        com.google.android.material.snackbar.ContentViewCallback {

        override fun animateContentOut(delay: Int, duration: Int) {
            view.visibility = View.VISIBLE
        }

        override fun animateContentIn(delay: Int, duration: Int) {
            view.visibility = View.VISIBLE
        }

    }
}