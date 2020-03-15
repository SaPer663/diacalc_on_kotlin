package org.diacalc.android.components

import android.text.InputFilter
import android.widget.EditText

class FloatEditText : EditText {
    //new DecimalFormatSymbols().getDecimalSeparator();
    private var df: java.text.DecimalFormat? = null

    constructor(context: android.content.Context?) : super(context) {
        init()
    }

    constructor(context: android.content.Context?, attrs: android.util.AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: android.content.Context?, attrs: android.util.AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        setZeroes(1)
    }

    fun setZeroes(z: Int) { //z - количество знаков после запятой
        this.filters = arrayOf<InputFilter>(FloatInputFilter(z))
        var pattern = "0"
        if (z > 0) {
            pattern += DECIMAL_SEPARATOR
            for (i in 0 until z) {
                pattern += "0"
            }
        }
        val f: java.text.NumberFormat = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (f is java.text.DecimalFormat) {
            df = f as java.text.DecimalFormat
            df!!.applyPattern(pattern)
        }
    }//Не нуль
    //вместо нуля ставим пустую строку
//Log.e("FloatEditText", ex.getMessage());

    //Не нуль
    //setText(df.format(vl));
    //вместо нуля ставим пустую строку
    //String st = getText().toString().replace(DECIMAL_SEPARATOR, '.');
    var value: Float
        get() { //String st = getText().toString().replace(DECIMAL_SEPARATOR, '.');
            var vl = 0f
            try {
                vl = getText().toString().trim { it <= ' ' }.toFloat()
            } catch (ex: java.lang.Exception) { //Log.e("FloatEditText", ex.getMessage());
            }
            if (java.lang.Math.abs(vl - 0) > 0.0001) { //Не нуль
                setText(df?.format(vl.toDouble()))
            } else setText("") //вместо нуля ставим пустую строку
            return vl
        }
        set(vl) {
            if (java.lang.Math.abs(vl - 0) > 0.0001) { //Не нуль
                if (df != null) {
                    setText(df!!.format(vl.toDouble()))
                } else {
                    setText("" + vl)
                }
                //setText(df.format(vl));
            } else setText("") //вместо нуля ставим пустую строку
            setSelection(getText().length)
        }

    companion object {
        private const val DECIMAL_SEPARATOR = '.'
    }
}