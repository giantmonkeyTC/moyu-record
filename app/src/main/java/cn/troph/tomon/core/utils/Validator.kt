package cn.troph.tomon.core.utils

import android.text.TextUtils
import java.util.regex.Pattern

object Patterns {
    val USER_NAME: Pattern = Pattern.compile("[a-zA-Z0-9_.-]+")
    val FULL_NAME: Pattern = Pattern.compile("[a-zA-Z0-9_.-]+#[0-9]{4}]")
}

object Validator {

    fun isFullName(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && Patterns.FULL_NAME.matcher(value).matches()
    }

    fun isUserName(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && Patterns.USER_NAME.matcher(value).matches()
    }

    fun isEmail(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && android.util.Patterns.EMAIL_ADDRESS.matcher(value)
            .matches()
    }

    fun isPhone(value: String?): Boolean {
        return !TextUtils.isEmpty(value) && android.util.Patterns.PHONE.matcher(value).matches()
    }
}