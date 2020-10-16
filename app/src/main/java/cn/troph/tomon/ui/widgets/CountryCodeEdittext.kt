package cn.troph.tomon.ui.widgets

import android.content.Context
import android.content.res.Configuration
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import cn.troph.tomon.R
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.country_code_edittext.view.*

class CountryCodeEdittext (context: Context,attributeSet: AttributeSet): FrameLayout(context,attributeSet) {
    var countryCodeEdittext: View
    init {
        val inflater  = LayoutInflater.from(context)
        countryCodeEdittext = inflater.inflate(R.layout.country_code_edittext,null,false)
        addView(countryCodeEdittext)
        edittextConfig()
        countryCodePickerConfig()
    }
    fun getEdittext(): EditText {
        return countryCodeEdittext.country_code_edittext
    }
    private fun edittextConfig(){
        getEdittext().inputType = InputType.TYPE_CLASS_PHONE
        getEdittext().setRawInputType(Configuration.KEYBOARD_QWERTY)
    }
    fun getCountryCodePicker(): CountryCodePicker {
        return countryCodeEdittext.countryPicker
    }
    private fun countryCodePickerConfig(){
        getCountryCodePicker().registerCarrierNumberEditText(getEdittext())
    }
    fun text(): String {
        return getCountryCodePicker().fullNumberWithPlus
    }
}