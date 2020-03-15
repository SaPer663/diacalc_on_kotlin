package org.diacalc.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.diacalc.android.manager.DatabaseManager
import kotlin.random.Random.Default.Companion

class DCJStart : Activity() {
    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        setContentView(R.layout.main)
        android.util.Log.i(DCJStart.Companion.DCJ_TAG, "on create!!!")
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_MENU) {
            showDialog(DCJStart.Companion.DIALOG_ABOUTBOX_ID)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun onMenuBtnClick(v: android.view.View?) { //Запускаем форму меню
        val intent = Intent()
        intent.setClass(this, MenuForm::class.java)
        startActivity(intent)
    }

    fun onSettingsBtnClick(v: android.view.View?) { //Запускаем настройки
        val intent = Intent()
        intent.setClass(this, SettingsForm::class.java)
        startActivity(intent)
    }

    fun onProductsBtnClick(v: android.view.View?) { //Запускаем продукты
        val intent = Intent()
        intent.setClass(this, ProdForm::class.java)
        startActivity(intent)
    }

    override fun onCreateDialog(id: Int): android.app.Dialog? {
        return when (id) {
            DCJStart.Companion.DIALOG_ABOUTBOX_ID -> createAboutBox()
            else -> null
        }
    }

    private fun createAboutBox(): android.app.Dialog {
        val dialog: android.app.Dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.aboutbox)
        dialog.setTitle(getString(R.string.aboutBoxTitle))
        return dialog
    }

    override fun onPause() {
        super.onPause()
        //Сохраняем
        if (isFinishing) {
            val mgr = DatabaseManager(this)
            //Нормальный выход из приложения, сохраняем данные
            (application as DataPocket).storeMenuProds(mgr)
            (application as DataPocket).storeUser(mgr)
            //Потом сбрасывам в ноль указатели, что заставит при
//повторном обращении к ним загрузить их снова из БД
            (this.application as DataPocket).setAllPointers2Null()
        }
    }
    companion object  {
        private  val  DCJ_TAG:/*@@gxicbm@@*/kotlin.String? = "DCJmobile"
        private const val  DIALOG_ABOUTBOX_ID:/*@@dwkqiq@@*/Int = 0
    }
}
/*@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == SUB_ACTIVITY_REQUEST_CODE){
				//Bundle b = data.getExtras();
				//tv.setText(b.getString("TEXT"));
        }
	}*/
/*
public void btnSettingsClick(View v){
/ *Intent intent = new Intent();
intent.setClass(this, SettingsForm.class);

startActivity(intent);


finish();* /
/ *Toast
.makeText(DCJStart.this,"Hi! Bright Hub",
Toast.LENGTH_SHORT/Toast.LENGTH_LONG).show();
}* /
*/