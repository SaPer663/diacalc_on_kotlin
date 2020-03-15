package org.diacalc.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost.TabSpec;
import org.diacalc.android.components.FloatEditText;
import org.diacalc.android.manager.DatabaseManager;
import org.diacalc.android.maths.Sugar;
import org.diacalc.android.maths.User;



public class SettingsForm extends Activity{
	private static final String DEFAULT_SERVER = "http://diacalc.org/dbwork/";
	
	private EditText editLogin;
	private EditText editServer;
	private EditText editPass;
	
	private DataPocket dtPkt;
	private int currentTab = 0;
	private TabHost tabHost;
	
	private RadioButton rbWhole;
	private RadioButton rbPlasma;
	private RadioButton rbMmol;
	private RadioButton rbMgdl;
	
	private FloatEditText editTargetSh;
	private FloatEditText editLowSh;
	private FloatEditText editHiSh;
	
	private RadioButton rbOne;
	private CheckBox calcCoefByTime;
	private Spinner menuInfoVariant;
	
	private User user;
	private DatabaseManager mgr;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState!=null){
        	currentTab = savedInstanceState.getInt("currentTab");
        }
        
        setContentView(R.layout.settings);
        
        mgr = new DatabaseManager(this);
        
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();
        
        TabSpec spec1=tabHost.newTabSpec("TabInet");
        spec1.setContent(R.id.tabSettingsInet);
        spec1.setIndicator(getString(R.string.tabSettingsInet));
        
        TabSpec spec2=tabHost.newTabSpec("TabBlood");
        spec2.setIndicator(getString(R.string.tabSettingsBlood));
        spec2.setContent(R.id.tabSettingsBlood);

        TabSpec spec3=tabHost.newTabSpec("TabMenu");
        spec3.setIndicator(getString(R.string.tabSettingsMenu));
        spec3.setContent(R.id.tabSettingsMenu);

        tabHost.addTab(spec1);
        tabHost.addTab(spec2);
        tabHost.addTab(spec3);
        
        tabHost.setCurrentTab(currentTab);
        tabHost.setOnTabChangedListener(
        		new TabHost.OnTabChangeListener(){
        			public void onTabChanged(String st){
        				switch (currentTab){
        				case 0: saveInetData();//Сохраняем логин, пароль, адрес
        					break;
        				case 1: readSugarValues();//Считали значения сахаров
        					break;
        				case 2:
        					break;
        				}
        				currentTab = tabHost.getCurrentTab();
        			}
        		});
        dtPkt = (DataPocket)this.getApplication();
        user = dtPkt.getUser(mgr);
        
        editLogin = (EditText)findViewById(R.id.editSettingsLogin);
        editServer = (EditText)findViewById(R.id.editSettingsServer);
        editPass = (EditText)findViewById(R.id.editSettingsPass);
        
        rbWhole = (RadioButton)findViewById(R.id.rbSettingsWhole);
        rbPlasma = (RadioButton)findViewById(R.id.rbSettingsPlasma);
        rbMmol = (RadioButton)findViewById(R.id.rbSettingsMmol);
        rbMgdl = (RadioButton)findViewById(R.id.rbSettingsMgdl);
        
        editTargetSh = (FloatEditText)findViewById(R.id.editSettingsTargetSh);
        editLowSh = (FloatEditText)findViewById(R.id.editSettingsLowSh);
        editHiSh = (FloatEditText)findViewById(R.id.editSettingsHiSh);
        
        editLogin.setText(user.getLogin());
        editServer.setText(user.getServer());
        editPass.setText(user.getPass());
        
        menuInfoVariant = (Spinner)findViewById(R.id.spinnerSettingsMenuInfo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.menuSettingsInfoVariants, 
                	android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        menuInfoVariant.setAdapter(adapter);
        menuInfoVariant.setSelection(user.getMenuInfo());
        
        rbOne = (RadioButton)findViewById(R.id.rbtnSettingsOne);
        calcCoefByTime = (CheckBox)findViewById(R.id.chBxSettingsCoefTime);
        
        rbOne.setChecked(user.getRound()==User.ROUND_1);
        calcCoefByTime.setChecked(user.isTimeSense());
        
        rbPlasma.setChecked(user.isPlasma());
        rbWhole.setChecked(!user.isPlasma());
        rbMmol.setChecked(user.isMmol());
        rbMgdl.setChecked(!user.isMmol());
        
        fillSugars();
        
        rbOne.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton bt, boolean isChecked){
        		//тут вначале нужно считать данные из полей ввода
        		if (isChecked) user.setRound(User.ROUND_1);
        		else user.setRound(User.ROUND_05);
        	}
        	
        });
        calcCoefByTime.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton bt, boolean isChecked){
        		//тут вначале нужно считать данные из полей ввода
        		user.setTimeSense(isChecked);
        	}
        });
        menuInfoVariant.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				user.setMenuInfo(arg2);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0){}
        });
        rbWhole.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton bt, boolean isChecked){
        		//тут вначале нужно считать данные из полей ввода
        		readSugarValues();
        		user.setPlasma(!isChecked);
        		fillSugars();
        	}
        });
        rbMmol.setOnCheckedChangeListener(new OnCheckedChangeListener(){
        	public void onCheckedChanged(CompoundButton bt, boolean isChecked){
        		//тут вначале нужно считать данные из полей ввода
        		readSugarValues();
        		user.setMmol(isChecked);
        		fillSugars();
        	}
        });
        
    }
	private void readSugarValues(){
		Sugar s = new Sugar();
		s.setSugar(editTargetSh.getValue(), user.isMmol(), user.isPlasma());
		user.setTargetSugar(s.getValue());
		s.setSugar(editLowSh.getValue(), user.isMmol(), user.isPlasma());
		user.setLowSugar(s.getValue());
		s.setSugar(editHiSh.getValue(), user.isMmol(), user.isPlasma());
		user.setHiSugar(s.getValue());
	}
	private void fillSugars(){
		if (user.isMmol()){
			editTargetSh.setZeroes(1);
			editLowSh.setZeroes(1);
			editHiSh.setZeroes(1);
		}else{
			editTargetSh.setZeroes(0);
			editLowSh.setZeroes(0);
			editHiSh.setZeroes(0);
		}
		
		Sugar s = new Sugar();
        s.setValue(user.getTargetSugar());
        editTargetSh.setValue( s.getSugar(user.isMmol(), user.isPlasma()) );
        
        s.setValue(user.getLowSugar());
        editLowSh.setValue( s.getSugar(user.isMmol(), user.isPlasma()) );
        
        s.setValue(user.getHiSugar());
        editHiSh.setValue( s.getSugar(user.isMmol(), user.isPlasma()) );
	}
	
	private void saveInetData(){
		if (user.getLogin().equals(editLogin.getText().toString())&&
			user.getPass().equals(editPass.getText().toString())&&
			user.getServer().equals(editServer.getText().toString()))
			return; //Если ничего не поменялось, то ничего и не делаем
		//Надо тут проверить адрес на валидность
		String url = editServer.getText().toString();
        if (url.length()==0){
            url = DEFAULT_SERVER;
            editServer.setText(url);
        }
        else if (!url.endsWith("/")){
            url += "/";
            editServer.setText(url);
        }
		//Конец проверки
		user.setLogin(editLogin.getText().toString());
		user.setServer(url);
		user.setPass(editPass.getText().toString());
	}
	@Override
	public void onPause(){//Тут сохраняем данные
		super.onPause();
		switch(currentTab){
		case 0:saveInetData();
			break;
		case 1:readSugarValues();
			break;
		case 2:break;
		}
		//А вот тут уже сохраняем всего пользователя целиком в базу
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  savedInstanceState.putInt("currentTab", currentTab);
	  
	  super.onSaveInstanceState(savedInstanceState);
	}
	
	/*@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  currentTab = savedInstanceState.getInt("currentTab");
	  tabHost.setCurrentTab(currentTab);
	  Log.i(SET_TAG,"2tab should be "+currentTab);
	}*/
	
}
