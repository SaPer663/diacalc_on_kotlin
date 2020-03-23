package org.diacalc.android

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.View
import org.diacalc.android.manager.DatabaseManager

class DCJStart : Activity() {
    /** Called when the activity is first created.  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore UI state from the savedInstanceState.
        setContentView(R.layout.main)
        android.util.Log.i(DCJ_TAG, "on create!!!")
        val dbm = DatabaseManager(this)
        val db: SQLiteDatabase = dbm.writableDatabase
        dbm.onCreate(db)
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_MENU) {
            showDialog(DIALOG_ABOUTBOX_ID)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun onMenuBtnClick(v: View) { //Запускаем форму меню
        val intent = Intent()
        intent.setClass(this, MenuForm::class.java)
        startActivity(intent)
    }

    fun onSettingsBtnClick(v: View) { //Запускаем настройки
        val intent = Intent()
        intent.setClass(this, SettingsForm::class.java)
        startActivity(intent)
    }

    fun onProductsBtnClick(v: View) { //Запускаем продукты
        val intent = Intent()
        intent.setClass(this, ProdForm::class.java)
        startActivity(intent)
    }

    override fun onCreateDialog(id: Int): android.app.Dialog? {
        return when (id) {
            DIALOG_ABOUTBOX_ID -> createAboutBox()
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
            (application as DataPocket).saveProductMenuToBD(mgr)
            (application as DataPocket).saveUserToBD(mgr)
            //Потом сбрасывам в ноль указатели, что заставит при
//повторном обращении к ним загрузить их снова из БД
            (this.application as DataPocket).resettingToZeroPointers()
        }
    }
    companion object  {
        private  val  DCJ_TAG: String? = "DCJmobile"
        private const val  DIALOG_ABOUTBOX_ID: Int = 0
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