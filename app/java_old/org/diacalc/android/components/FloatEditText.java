package org.diacalc.android.components;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.text.InputFilter;


public class FloatEditText extends EditText {
	private final static char DECIMAL_SEPARATOR = '.';
		//new DecimalFormatSymbols().getDecimalSeparator();
	private DecimalFormat df = null;
	
	public FloatEditText(Context context) {
		super(context);
		init();
	}
	public FloatEditText(Context context, AttributeSet attrs) {
		super(context,attrs);
		init();
	}
	public FloatEditText(Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		init();
	}
	private void init(){
		setZeroes(1);
	}
	
	public void setZeroes(int z){//z - количество знаков после запятой
		setFilters(new InputFilter[] {new FloatInputFilter(z)});
		String pattern = "0";
		if (z>0){
			pattern += DECIMAL_SEPARATOR;
			for(int i=0;i<z;i++){
				pattern += "0";
			}
		}
		
		NumberFormat f = NumberFormat.getInstance(Locale.US);
		if (f instanceof DecimalFormat) {
			df = (DecimalFormat)f;
			df.applyPattern(pattern);
		}
	}
	
	public float getValue(){
		//String st = getText().toString().replace(DECIMAL_SEPARATOR, '.');
		float vl = 0f;
		try{
			vl = Float.parseFloat(getText().toString().trim());
		}catch(Exception ex){
			//Log.e("FloatEditText", ex.getMessage());
		}
		if (Math.abs(vl-0)>0.0001){//Не нуль
				setText(df.format(vl));
		}else 	setText("");//вместо нуля ставим пустую строку
		return vl;
	}
	public void setValue(float vl){
		if (Math.abs(vl-0)>0.0001){//Не нуль
			if (df!=null){
				setText(df.format(vl));
			}else{
				setText(""+vl);
			}
			//setText(df.format(vl));
		}else 	setText("");//вместо нуля ставим пустую строку
		
		setSelection(getText().length());
	}
	
}
