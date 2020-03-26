package cn.troph.tomon.core.utils

import android.text.TextUtils

object Validator {
    fun isEmail(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && android.util.Patterns.EMAIL_ADDRESS.matcher(value)
            .matches()
    }

    fun isPhone(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && android.util.Patterns.PHONE.matcher(value).matches()
    }
}