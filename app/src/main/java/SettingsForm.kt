package org.diacalc.android

import android.app.Activity
import android.os.Bundle
import android.widget.*
import org.diacalc.android.components.FloatEditText
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.Sugar

class SettingsForm : Activity() {
    private var editLogin: EditText? = null
    private var editServer: EditText? = null
    private var editPass: EditText? = null
    private var dtPkt: DataPocket? = null
    private var currentTab = 0
    private var tabHost: TabHost? = null
    private var rbWhole: RadioButton? = null
    private var rbPlasma: RadioButton? = null
    private var rbMmol: RadioButton? = null
    private var rbMgdl: RadioButton? = null
    private var editTargetSh: FloatEditText? = null
    private var editLowSh: FloatEditText? = null
    private var editHiSh: FloatEditText? = null
    private var rbOne: RadioButton? = null
    private var calcCoefByTime: CheckBox? = null
    private var menuInfoVariant: Spinner? = null
    private var user: org.diacalc.android.maths.User? = null
    private var mgr: DatabaseManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("currentTab")
        }
        setContentView(R.layout.settings)
        mgr = DatabaseManager(this)
        tabHost = findViewById<android.view.View>(R.id.tabHost) as TabHost?
        tabHost?.setup()
        val spec1: TabHost.TabSpec = tabHost!!.newTabSpec("TabInet")
        spec1.setContent(R.id.tabSettingsInet)
        spec1.setIndicator(getString(R.string.tabSettingsInet))
        val spec2: TabHost.TabSpec = tabHost!!.newTabSpec("TabBlood")
        spec2.setIndicator(getString(R.string.tabSettingsBlood))
        spec2.setContent(R.id.tabSettingsBlood)
        val spec3: TabHost.TabSpec = tabHost!!.newTabSpec("TabMenu")
        spec3.setIndicator(getString(R.string.tabSettingsMenu))
        spec3.setContent(R.id.tabSettingsMenu)
        tabHost!!.addTab(spec1)
        tabHost!!.addTab(spec2)
        tabHost!!.addTab(spec3)
        tabHost!!.currentTab = currentTab
        tabHost!!.setOnTabChangedListener {
            when (currentTab) {
                0 -> saveInetData() //Сохраняем логин, пароль, адрес
                1 -> readSugarValues() //Считали значения сахаров
                2 -> {
                }
            }
            currentTab = tabHost!!.currentTab
        }
        dtPkt = this.application as DataPocket?
        user = dtPkt?.getUserFromBD(mgr!!)
        editLogin = findViewById<android.view.View>(R.id.editSettingsLogin) as EditText?
        editServer = findViewById<android.view.View>(R.id.editSettingsServer) as EditText?
        editPass = findViewById<android.view.View>(R.id.editSettingsPass) as EditText?
        rbWhole = findViewById<android.view.View>(R.id.rbSettingsWhole) as RadioButton?
        rbPlasma = findViewById<android.view.View>(R.id.rbSettingsPlasma) as RadioButton?
        rbMmol = findViewById<android.view.View>(R.id.rbSettingsMmol) as RadioButton?
        rbMgdl = findViewById<android.view.View>(R.id.rbSettingsMgdl) as RadioButton?
        editTargetSh = findViewById<android.view.View>(R.id.editSettingsTargetSh) as FloatEditText?
        editLowSh = findViewById<android.view.View>(R.id.editSettingsLowSh) as FloatEditText?
        editHiSh = findViewById<android.view.View>(R.id.editSettingsHiSh) as FloatEditText?
        editLogin?.setText(user?.login)
        editServer?.setText(user?.server)
        editPass?.setText(user?.pass)
        menuInfoVariant = findViewById<android.view.View>(R.id.spinnerSettingsMenuInfo) as Spinner?
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                this, R.array.menuSettingsInfoVariants,
                android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        menuInfoVariant?.adapter = adapter
        user?.menuInfo?.let { menuInfoVariant?.setSelection(it) }
        rbOne = findViewById<android.view.View>(R.id.rbtnSettingsOne) as RadioButton?
        calcCoefByTime = findViewById<android.view.View>(R.id.chBxSettingsCoefTime) as CheckBox?
        rbOne?.isChecked = user!!.round == org.diacalc.android.maths.User.ROUND_1
        calcCoefByTime?.isChecked = user!!.isTimeSense
        rbPlasma?.isChecked = user!!.isPlasma
        rbWhole?.isChecked = !user!!.isPlasma
        rbMmol?.isChecked = user!!.isMmol
        rbMgdl?.isChecked = !user!!.isMmol
        fillSugars()
        rbOne?.setOnCheckedChangeListener { bt, isChecked -> //тут вначале нужно считать данные из полей ввода
            if (isChecked) user!!.round = (org.diacalc.android.maths.User.ROUND_1)
            else user!!.round =(org.diacalc.android.maths.User.ROUND_05)
        }
        calcCoefByTime?.setOnCheckedChangeListener { bt, isChecked -> //тут вначале нужно считать данные из полей ввода
            user!!.isTimeSense = isChecked
        }
        menuInfoVariant?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(arg0: AdapterView<*>?, arg1: android.view.View,
                                        arg2: Int, arg3: Long) {
                user!!.menuInfo = arg2
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
        rbWhole?.setOnCheckedChangeListener { bt, isChecked -> //тут вначале нужно считать данные из полей ввода
            readSugarValues()
            user!!.isPlasma = !isChecked
            fillSugars()
        }
        rbMmol?.setOnCheckedChangeListener { bt, isChecked -> //тут вначале нужно считать данные из полей ввода
            readSugarValues()
            user!!.isMmol = isChecked
            fillSugars()
        }
    }

    private fun readSugarValues() {
        val s = Sugar()
        user?.isMmol?.let { editTargetSh?.formattedValue?.let { it1 -> s.setSugar(it1, it, user!!.isPlasma) } }
        user?.targetSugar ?: s.value
        user?.isPlasma?.let { editLowSh?.formattedValue?.let { it1 -> s.setSugar(it1, user!!.isMmol, it) } }
        user?.lowSugar ?: s.value
        editHiSh?.formattedValue?.let { user?.isPlasma?.let { it1 -> s.setSugar(it, user!!.isMmol, it1) } }
        user?.hiSugar ?: s.value
    }

    private fun fillSugars() {
        if (user?.isMmol!!) {
            editTargetSh?.setZeroesAfterDecimalPoint(1)
            editLowSh?.setZeroesAfterDecimalPoint(1)
            editHiSh?.setZeroesAfterDecimalPoint(1)
        } else {
            editTargetSh?.setZeroesAfterDecimalPoint(0)
            editLowSh?.setZeroesAfterDecimalPoint(0)
            editHiSh?.setZeroesAfterDecimalPoint(0)
        }
        val s = Sugar()
        s.value = user!!.targetSugar
        editTargetSh?.formattedValue ?: (s.getSugar(user!!.isMmol, user!!.isPlasma))
        s.value = user!!.lowSugar
        editLowSh?.formattedValue ?: (s.getSugar(user!!.isMmol, user!!.isPlasma))
        s.value = user!!.hiSugar
        editHiSh?.formattedValue ?: (s.getSugar(user!!.isMmol, user!!.isPlasma))
    }

    private fun saveInetData() {
        if (user!!.login == editLogin?.text.toString() &&
            user!!.pass == editPass?.text.toString() &&
            user!!.server == editServer?.text.toString()) return  //Если ничего не поменялось, то ничего и не делаем
        //Надо тут проверить адрес на валидность
        var url: String = editServer?.text.toString()
        if (url.isEmpty()) {
            url = DEFAULT_SERVER
            editServer?.setText(url)
        } else if (!url.endsWith("/")) {
            url += "/"
            editServer?.setText(url)
        }
        //Конец проверки
        user!!.login = editLogin?.text.toString()
        user!!.server = url
        user!!.pass = editPass?.text.toString()
    }

    override fun onPause() { //Тут сохраняем данные
        super.onPause()
        when (currentTab) {
            0 -> saveInetData()
            1 -> readSugarValues()
            2 -> {
            }
        }
        //А вот тут уже сохраняем всего пользователя целиком в базу
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) { // Save UI state changes to the savedInstanceState.
// This bundle will be passed to onCreate if the process is
// killed and restarted.
        savedInstanceState.putInt("currentTab", currentTab)
        super.onSaveInstanceState(savedInstanceState)
    } /*@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  currentTab = savedInstanceState.getInt("currentTab");
	  tabHost.setCurrentTab(currentTab);
	  Log.i(SET_TAG,"2tab should be "+currentTab);
	}*/

    companion object {
        private const val DEFAULT_SERVER = "http://diacalc.org/dbwork/"
    }
}