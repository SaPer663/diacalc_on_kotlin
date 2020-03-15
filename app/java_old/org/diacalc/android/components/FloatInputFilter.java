package org.diacalc.android.components;

import java.util.regex.Pattern;
import android.text.InputFilter;
import android.text.Spanned;


public class FloatInputFilter implements InputFilter {
	private final static char DECIMAL_SEPARATOR = '.';
		//new DecimalFormatSymbols().getDecimalSeparator();
	private String pattern;
	
	
	public FloatInputFilter(int z){//z - количество запятых после знака
		pattern = "[0-9]+";
		if (z>0){
			pattern += "(["+ DECIMAL_SEPARATOR + 
				"]{1}||["+ DECIMAL_SEPARATOR + "]{1}[0-9]{";
			for(int i=0;i<z;i++){
				pattern += ""+(i+1)+",";
			}
			pattern = pattern.substring(0, pattern.length()-1) + "})?";
			
		}
	}
	@Override
	public CharSequence filter(CharSequence source, int start, int end,
	      Spanned dest, int dstart, int dend) {
	    String checkedText = dest.subSequence(0, dstart).toString() + 
	    	source.subSequence(start, end) + 
	    	dest.subSequence(dend,dest.length()).toString();

	    if (!Pattern.matches(pattern, checkedText)) {
	      return "";
	    }
	    
	    return null;
	}
}
