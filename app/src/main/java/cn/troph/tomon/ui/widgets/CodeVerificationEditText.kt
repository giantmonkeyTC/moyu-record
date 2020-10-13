package cn.troph.tomon.ui.widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatEditText
import cn.troph.tomon.R
import cn.troph.tomon.core.utils.DensityUtil
import cn.troph.tomon.core.utils.KeyboardUtils


class CodeVerificationEditText(context: Context, attrs: AttributeSet) :
    AppCompatEditText(context, attrs) {

    private var maxLength = 4
    private var mStrokeWidth = 44
    private var mStrokeHeight = 0
    private var mStrokePadding = 16
    private val mRect = Rect()
    private lateinit var mStrokeDrawable: Drawable
    private lateinit var mFocusedDrawable: Drawable
    private var mOnInputFinishListener: OnTextFinishListener? = null
    private var xyArray: Array<Array<Float>>
    val textPaint = Paint()


    interface OnTextFinishListener {
        fun onTextFinish(text: CharSequence?, length: Int)
    }


    init {
        setMaxLength(maxLength)
        isLongClickable = false
        setBackgroundColor(Color.TRANSPARENT)
        setRawInputType(Configuration.KEYBOARD_QWERTY)
        inputType = InputType.TYPE_CLASS_NUMBER
        isCursorVisible = false
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CodeVerificationEditText)
        val indexCount = typedArray.indexCount
        textPaint.color = currentTextColor
        for (i in 0 until indexCount) {
            val index = typedArray.getIndex(i)
            if (index == R.styleable.CodeVerificationEditText_strokeHeight)
                mStrokeHeight = typedArray.getDimension(index, 44f).toInt()
            else if (index == R.styleable.CodeVerificationEditText_strokeWidth)
                mStrokeWidth = typedArray.getDimension(index, 44f).toInt()
            else if (index == R.styleable.CodeVerificationEditText_strokePadding)
                mStrokePadding = typedArray.getDimension(index, 16f).toInt()
            else if (index == R.styleable.CodeVerificationEditText_strokeLength)
                maxLength = typedArray.getInt(index, 4)
            else if (index == R.styleable.CodeVerificationEditText_strokeDrawable)
                mStrokeDrawable = typedArray.getDrawable(index)!!
            else if (index == R.styleable.CodeVerificationEditText_focusedDrawable)
                mFocusedDrawable = typedArray.getDrawable(index)!!
        }
        mRect.left = 0
        mRect.top = DensityUtil.dip2px(context, 44f) - mStrokeHeight
        mRect.right = mStrokeWidth
        mRect.bottom = DensityUtil.dip2px(context, 44f)
        mStrokeDrawable.setBounds(mRect)
        mFocusedDrawable.setBounds(mRect)
        xyArray = Array(maxLength) {
            arrayOf(
                (mStrokeWidth / 2 + (mStrokeWidth + mStrokePadding) * it).toFloat() - DensityUtil.dip2px(
                    context,
                    15f
                ).toFloat() / 2,
                (DensityUtil.dip2px(context, 30f)).toFloat()
            )
        }
        typedArray.recycle()
    }

    private fun setMaxLength(maxLength: Int) {
        if (maxLength >= 0)
            filters = arrayOf(InputFilter.LengthFilter(maxLength))
        else
            filters = arrayOf()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return false
    }

    override fun onDraw(canvas: Canvas?) {
        drawBackground(canvas)
        drawText(canvas)
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        val textLength = editableText.length
        if (textLength == maxLength) {
            hideSoftInput()
            mOnInputFinishListener?.onTextFinish(editableText.toString(), maxLength)
        }
    }

    fun hideSoftInput() {
        val imm: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun drawText(canvas: Canvas?) {
        if (canvas != null) {
            val count = canvas.saveCount
            canvas.translate(0f, 0f)
            val length = editableText.length
            for (i in 0 until length) {
                val text = editableText[i].toString()
                textPaint.textSize = DensityUtil.dip2px(context, 25f).toFloat()
                textPaint.color = currentTextColor
                canvas.drawText(text, xyArray[i][0], xyArray[i][1], textPaint)
            }
            canvas.restoreToCount(count)
        }
    }

    private fun drawBackground(canvas: Canvas?) {
        if (canvas != null) {
            val activatedIndex = Math.max(0, editableText.length)
            val count = canvas?.saveCount
            canvas?.save()
            for (i in 0 until maxLength) {
                if (i >= activatedIndex)
                    mStrokeDrawable?.let {
                        it.draw(canvas)
                        val dx = mRect.right + mStrokePadding
                        canvas.save()
                        canvas.translate(dx.toFloat(), 0f)
                    }
                else
                    mFocusedDrawable?.let {
                        it.draw(canvas)
                        val dx = mRect.right + mStrokePadding
                        canvas.save()
                        canvas.translate(dx.toFloat(), 0f)
                    }
            }
            canvas.restoreToCount(count)
            canvas.translate(0f, 0f)
        }

    }
}