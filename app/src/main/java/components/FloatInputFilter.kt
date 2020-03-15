package org.diacalc.android.components

import android.text.InputFilter
import android.text.Spanned

class FloatInputFilter(z: Int) : InputFilter {
    //new DecimalFormatSymbols().getDecimalSeparator();
    private var pattern = "[0-9]+"

    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): String? {
        val checkedText: String = dest.subSequence(0, dstart).toString() +
                source.subSequence(start, end) +
                dest.subSequence(dend, dest.length).toString()
        return if (!java.util.regex.Pattern.matches(pattern, checkedText)) {
            ""
        } else null
    }

    companion object {
        private const val DECIMAL_SEPARATOR = '.'
    }

    init { //z - количество запятых после знака
        if (z > 0) {
            pattern += "([" + DECIMAL_SEPARATOR +
                    "]{1}||[" + DECIMAL_SEPARATOR + "]{1}[0-9]{"
            for (i in 0 until z) {
                pattern += "" + (i + 1) + ","
            }
            pattern = pattern.substring(0, pattern.length - 1) + "})?"
        }
    }
}