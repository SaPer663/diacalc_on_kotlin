package org.diacalc.android.components

import android.text.InputFilter
import android.widget.EditText
import kotlin.math.abs

class FloatEditText : EditText {
    //new DecimalFormatSymbols().getDecimalSeparator();
    private var decimalFormat: java.text.DecimalFormat = java.text.DecimalFormat()

    constructor(context: android.content.Context) : super(context) {
        init()
    }

    constructor(context: android.content.Context, attrs: android.util.AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: android.content.Context, attrs: android.util.AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        setZeroesAfterDecimalPoint(1)
    }

    fun setZeroesAfterDecimalPoint(numberOfZeroes: Int) { //z - количество знаков после запятой
        this.filters = arrayOf<InputFilter>(FloatInputFilter(numberOfZeroes))
        var pattern = "0"
        if (numberOfZeroes > 0) {
            pattern += DECIMAL_SEPARATOR
            for (i in 0 until numberOfZeroes) {
                pattern += "0"
            }
        }
        val format: java.text.NumberFormat = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (format is java.text.DecimalFormat) {
            decimalFormat = format
            decimalFormat.applyPattern(pattern)
        }
    }
    var formattedValue: Float
        get() { //String st = getText().toString().replace(DECIMAL_SEPARATOR, '.');
            var rezult = 0f
            try {
                rezult = text.toString().trim().toFloat()
            } catch (ex: java.lang.Exception) { //Log.e("FloatEditText", ex.getMessage());
            }
            if (abs(rezult - 0) > 0.0001) { //Не нуль
                setText(decimalFormat.format(rezult.toDouble()))
            } else setText("") //вместо нуля ставим пустую строку
            return rezult
        }
        set(value) {
            if (abs(value - 0) > 0.0001) { //Не нуль
                setText(decimalFormat.format(value.toDouble()))
                //setText(df.format(vl));
            }
            else setText("") //вместо нуля ставим пустую строку
            setSelection(text.length)
        }

    companion object {
        private const val DECIMAL_SEPARATOR = '.'
    }
}
//Не нуль
//вместо нуля ставим пустую строку
//Log.e("FloatEditText", ex.getMessage());

//Не нуль
//setText(df.format(vl));
//вместо нуля ставим пустую строку
//String st = getText().toString().replace(DECIMAL_SEPARATOR, '.');
