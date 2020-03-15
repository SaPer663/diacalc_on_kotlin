package org.diacalc.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import org.diacalc.android.components.FloatEditText
import org.diacalc.android.internet.DoingPost
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.DPS
import org.diacalc.android.maths.Dose
import org.diacalc.android.maths.Factors
import org.diacalc.android.maths.Sugar
import org.diacalc.android.products.ProductInMenu
import org.diacalc.android.products.ProductW
import java.text.DecimalFormat
import java.util.HashMap
import kotlin.math.abs
import kotlin.math.floor

class MenuForm : Activity() {
    //private ProgressDialog m_ProgressDialog = null;
    private val rows: java.util.ArrayList<RowHolder> = java.util.ArrayList()
    //private Runnable viewOrders;
    private var dtPkt: DataPocket? = null
    private var user: org.diacalc.android.maths.User? = null
    private var root: LinearLayout? = null //Панель в которой находится список
    private var textBD: TextView? = null
    private var textMD: TextView? = null
    private var textSumD: TextView? = null
    private var textInfo: TextView? = null
    private var df: DecimalFormat? = null
    private var df0: DecimalFormat? = null
    private var df00: DecimalFormat? = null
    private var sum: ProductW? = null  //Что бы не дергать каждый раз меню для округления
                                       //для использования в отдельном потоке
    private var iProds: java.util.ArrayList<ProductInMenu>? = null
    private var iSnack: java.util.ArrayList<ProductInMenu>? = null
    private var iUs: org.diacalc.android.maths.User? = null
    private var iMsg = ""
    private var iProgressdialog: android.app.ProgressDialog? = null
    private var mgr: DatabaseManager? = null
    /* Работа с диалогами
     */
    override fun onCreateDialog(id: Int): android.app.Dialog? {
        return when (id) {
            DIALOG_COEFS_ID -> createCoefDlg()
            DIALOG_DRS_ID -> createDRSDlg()
            else -> null
        }
    }

    private fun createDRSDlg(): android.app.Dialog {
        val dialog: android.app.Dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.menu_drs_dlg)
        dialog.setTitle(dialog.getContext().getString(R.string.SugarDialogTitle))
        var zeros = 0
        if (user!!.isMmol) zeros = 1
        val fldS1: FloatEditText = dialog.findViewById(R.id.editMenuDlgS1) as FloatEditText
        fldS1.setZeroes(zeros)
        val fldS2: FloatEditText = dialog.findViewById(R.id.editMenuDlgS2) as FloatEditText
        fldS2.setZeroes(zeros)
        val fldOUV: FloatEditText = dialog.findViewById(R.id.editMenuDlgOUV) as FloatEditText
        fldOUV.setZeroes(zeros + 1)
        dialog.setOnDismissListener { // TODO Auto-generated method stub
            val s = Sugar()
            s.setSugar(fldOUV.value, user!!.isMmol, user!!.isPlasma)
            user!!.factors.k3 = s.value
            s.setSugar(fldS1.value, user!!.isMmol, user!!.isPlasma)
            user!!.s1 = s.value
            s.setSugar(fldS2.value, user!!.isMmol, user!!.isPlasma)
            user!!.s2 = s.value
            //Где то тут надо проверить, считать дальше или
//предупредить о быстром снижении сахаров
            if (user!!.s1 - user!!.s2 > 5f) {
                val s2: Float = user!!.s1 - 5f
                val d: DecimalFormat? = if (user!!.isMmol) df0 else df
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MenuForm)
                if (d != null) {
                    builder.setMessage(getString(R.string.sugarFastAlert) + " " +
                                    d.format(Sugar(s2).getSugar(user!!.isMmol, user!!.isPlasma).toDouble())
                            )
                            .setCancelable(false)
                            .setPositiveButton(this@MenuForm.getString(R.string.btnOk)
                            ) { dialog, id -> //Нажали да
                                user!!.s2 = s2
                                fillSugarsButton()
                                calcMenu()
                            }
                            .setNegativeButton(this@MenuForm.getString(R.string.btnNo),
                                    null)
                } //Кнопку нет не надо слушать
                val alert: AlertDialog = builder.create()
                alert.show()
            }
            calcMenu()
            //Надо еще значения в кнопки записать
            fillSugarsButton()
        }
        val btnOk: android.widget.Button = dialog.findViewById(R.id.btnMenuDRSDlgOk) as android.widget.Button
        btnOk.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    private fun createCoefDlg(): android.app.Dialog {
        val dialog: android.app.Dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.menu_coefs_dlg)
        dialog.setTitle(getString(R.string.SugarDialogTitle))
        val fldK1: FloatEditText = dialog.findViewById(R.id.editMenuDlgK1) as FloatEditText
        fldK1.setZeroes(2)
        val fldK2: FloatEditText = dialog.findViewById(R.id.editMenuDlgK2) as FloatEditText
        fldK2.setZeroes(2)
        val fldBE: FloatEditText = dialog.findViewById(R.id.editMenuDlgBE) as FloatEditText
        fldBE.setZeroes(0)
        dialog.setOnDismissListener { // TODO Auto-generated method stub
            user!!.factors.setK1XE(fldK1.value, fldBE.value,
                    Factors.DIRECT)
            user!!.factors.setK2(fldK2.value)
            //пересчитываем
            calcMenu()
            //Надо еще значения в кнопки записать
            fillButtonCoef()
        }
        val btnOk: android.widget.Button = dialog.findViewById(R.id.btnMenuCoefDlgOk) as android.widget.Button
        btnOk.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    //Подготавливаем диалоги
    override fun onPrepareDialog(id: Int, dialog: android.app.Dialog) {
        when (id) {
            DIALOG_COEFS_ID -> {
                val fldK1: FloatEditText = dialog.findViewById(R.id.editMenuDlgK1) as FloatEditText
                fldK1.value = (user!!.factors.getK1(Factors.DIRECT))
                val fldK2: FloatEditText = dialog.findViewById(R.id.editMenuDlgK2) as FloatEditText
                fldK2.value = (user!!.factors.getK2())
                val fldBE: FloatEditText = dialog.findViewById(R.id.editMenuDlgBE) as FloatEditText
                fldBE.value = (user!!.factors.getBE(Factors.DIRECT))
            }
            DIALOG_DRS_ID -> {
                val fldS1: FloatEditText = dialog.findViewById(R.id.editMenuDlgS1) as FloatEditText
                fldS1.value = (Sugar(user!!.s1).getSugar(user!!.isMmol,
                        user!!.isPlasma))
                val fldS2: FloatEditText = dialog.findViewById(R.id.editMenuDlgS2) as FloatEditText
                fldS2.value = (Sugar(user!!.s2).getSugar(user!!.isMmol,
                        user!!.isPlasma))
                val fldOUV: FloatEditText = dialog.findViewById(R.id.editMenuDlgOUV) as FloatEditText
                fldOUV.value = (Sugar(user!!.factors.k3)
                        .getSugar(user!!.isMmol,
                                user!!.isPlasma))
            }
            else -> {
            }
        }
    }

    //Показываем диалоги
    fun onClickButtonMenuCoef(v: android.view.View?) {
        validyRowsValue()
        showDialog(DIALOG_COEFS_ID)
    }

    fun onClickButtonMenuDRS(v: android.view.View?) {
        validyRowsValue()
        showDialog(DIALOG_DRS_ID)
    }

    private fun validyRowsValue() { //Проверяем, что пользователь видит то же значение, что и установлено
//в весе продукта
        for (i in rows.indices) {
            if (abs(rows[i].weight!!.value - rows[i].prod!!.getWeight()) > 0.001) {
                rows[i].prod!!.setWeight(rows[i].weight!!.value)
            }
        }
    }

    ///Строим основное окно
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)
        root = findViewById<android.view.View>(R.id.listPaneMenu) as LinearLayout?
        mgr = DatabaseManager(this)
        dtPkt = this.application as DataPocket?
        user = dtPkt?.getUser(mgr!!)
        var f: java.text.NumberFormat = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (f is DecimalFormat) {
            df0 = f as DecimalFormat
            df0!!.applyPattern("0.0")
        }
        f = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (f is DecimalFormat) {
            df = f as DecimalFormat
            df!!.applyPattern("0")
        }
        f = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (f is DecimalFormat) {
            df00 = f as DecimalFormat
            df00!!.applyPattern("0.00")
        }
        for (i in dtPkt?.getMenuProds(mgr!!)!!.indices) {
            addRow(dtPkt!!.getMenuProds(mgr!!)!![i])
        }
        textBD = findViewById<android.view.View>(R.id.textMenuBD) as TextView?
        textMD = findViewById<android.view.View>(R.id.textMenuMD) as TextView?
        textSumD = findViewById<android.view.View>(R.id.textMenuSum) as TextView?
        textInfo = findViewById<android.view.View>(R.id.textMenuInfo) as TextView?
        fillButtonCoef()
        fillSugarsButton()
        calcMenu()
    }

    override fun onPause() { //Тут сохраняем данные
        super.onPause()
        if (dtPkt?.isNeed2SaveMenu!!) {
            android.util.Log.i("Menu", "need to save")
            dtPkt!!.getMenuProds(mgr!!)!!.clear()
            android.util.Log.i("Menu", "" + dtPkt!!.getMenuProds(mgr!!)!!.size)
            for (i in rows.indices) {
                rows[i].prod?.let { dtPkt!!.getMenuProds(mgr!!)!!.add(it) }
            }
            android.util.Log.i("Menu", "" + dtPkt!!.getMenuProds(mgr!!)!!.size)
        }
    }

    fun onStarMenuButtonClick(v: android.view.View?) {
        val intent = Intent()
        intent.setClass(baseContext, ProdForm::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean { // Обработка меню
        return when (item.itemId) {
            R.id.deleteOldMenu -> {
                deleteOldFromMenu()
                true
            }
            R.id.clearMenu -> {
                clearMenu()
                true
            }
            R.id.loadMenu -> {
                downloadMenu()
                true
            }
            R.id.uploadMenu -> {
                uploadMenu()
                true
            }
            R.id.createProdMenu -> true
            R.id.deleteRowMenuSub -> {
                deleteSelectedRow()
                true
            }
            R.id.roundDoseMenuSub -> {
                roundDose()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun uploadMenu() {
        val uploadingMenu: java.lang.Thread = object : java.lang.Thread() {
            override fun run() {
                if (rows.isEmpty()) return
                iProds = java.util.ArrayList()
                for (i in rows.indices) {
                    rows[i].prod?.let { iProds!!.add(it) }
                }
                iMsg = ""
                val iAnswer: HashMap<String?, Any?>? = DoingPost(user).sendMenu(iProds)
                if (iAnswer != null) {
                    if (iAnswer[DoingPost.ERROR] != null) { //Значит ошибка
                        if (iAnswer != null) {
                            iMsg = iAnswer[DoingPost.ERROR] as String
                        }
                    }
                }
                runOnUiThread(object : Runnable {
                    override fun run() {
                        iProgressdialog?.dismiss()
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MenuForm)
                        if (iMsg.isNotEmpty()) { //Выводим сообщение о ошибке
                            builder.setTitle(getString(R.string.errorTitle))
                                    .setMessage(getString(R.string.errorMsgMenuUpload).toString() + "\n" + iMsg)
                                    .setNeutralButton(getString(R.string.btnOk)) { dialog, id -> dialog.cancel() }
                        } else {
                            builder.setTitle(getString(R.string.menuUploadedTitle))
                                    .setMessage(getString(R.string.menuUploadedMsg))
                                    .setPositiveButton(getString(R.string.btnOk), null)
                        }
                        val alert: AlertDialog = builder.create()
                        alert.show()
                    }
                })
            }
        }
        iProgressdialog = android.app.ProgressDialog.show(this,
                getString(R.string.pleaseWait),
                getString(R.string.menuUploading), true)
        uploadingMenu.start()
    }

    private fun downloadMenu() {
        val loadingMenu: java.lang.Thread = object : java.lang.Thread() {
            override fun run() {
                iProds = null
                iSnack = null
                iUs = null
                iMsg = ""
                val iAnswer: HashMap<String?, Any?>? = DoingPost(user).requestMenu()
                if (iAnswer != null) {
                    if (iAnswer[DoingPost.ERROR] != null) { //Значит ошибка
                        iMsg = iAnswer.get(DoingPost.ERROR) as String
                    } else {
                        iProds = iAnswer[DoingPost.MENU_MENU] as java.util.ArrayList<ProductInMenu>
                        iSnack = iAnswer[DoingPost.MENU_SNACK] as java.util.ArrayList<ProductInMenu>
                        iUs = iAnswer[DoingPost.MENU_USER] as org.diacalc.android.maths.User
                    }
                }
                runOnUiThread {
                    iProgressdialog?.dismiss()
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@MenuForm)
                    if (iMsg.isNotEmpty()) { //Выводим сообщение о ошибке
                        builder.setTitle(getString(R.string.errorTitle))
                                .setMessage(getString(R.string.errorMsgMenuLoad).toString() + "\n" + iMsg)
                                .setNeutralButton(getString(R.string.btnOk)) { dialog, id -> dialog.cancel() }
                    } else {
                        builder.setTitle(getString(R.string.menuLoadedTitle))
                                .setMessage(getString(R.string.menuLoadedMsg).toString() +
                                        if (iSnack!!.size > 0) "\n" + getString(R.string.snackLoadedMsg) else "")
                                .setPositiveButton(getString(R.string.btnOk)) { dialog, id -> //Тут заменяем меню новым
                                    clearMenu()
                                    user = iUs
                                    for (i in iProds?.indices!!) {
                                        addRow(iProds!![i])
                                    }
                                    for (i in iSnack!!.indices) {
                                        addRow(iSnack!![i])
                                    }
                                    fillButtonCoef()
                                    fillSugarsButton()
                                    calcMenu()
                                }
                                .setNegativeButton(getString(R.string.btnNo)) { dialog, id -> //А тут ничего не делаем
                                    dialog.cancel()
                                }
                    }
                    val alert: AlertDialog = builder.create()
                    alert.show()
                }
            }
        }
        iProgressdialog = android.app.ProgressDialog.show(this,
                getString(R.string.pleaseWait),
                getString(R.string.menuLoading), true)
        loadingMenu.start()
    }

    private fun deleteOldFromMenu() {
        var renew = false
        for (i in rows.size downTo 1) {
            if (rows[i - 1].prod!!.id == -1) {
                root?.removeView(rows[i - 1].layout)
                rows.removeAt(i - 1)
                renew = true
            }
        }
        if (renew) {
            calcMenu()
            dtPkt?.setMenuNeed2Save()
        }
    }

    private fun clearMenu() {
        if (rows.isEmpty()) return
        dtPkt?.setMenuNeed2Save()
        if (!dtPkt?.isProductsNull!!) {
            for (it in mgr?.let { dtPkt!!.getProducts(it) }!!) {
                if (it.isSelected) it.isSelected = false
            }
        }
        rows.clear()
        root?.removeAllViews()
        calcMenu()
    }

    override fun onCreateContextMenu(menu: android.view.ContextMenu, v: android.view.View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = getMenuInflater()
        inflater.inflate(R.menu.menu_contxmenu, menu)
    }

    override fun onContextItemSelected(item: android.view.MenuItem): Boolean { //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        return when (item.getItemId()) {
            R.id.deleteRowMenu -> {
                //Удаляем выделенный ряд
                deleteSelectedRow()
                true
            }
            R.id.roundDoseMenu -> {
                //Округляем дозу
                roundDose()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private val selectedRow: Int
        private get() {
            for (i in 0 until root!!.childCount) {
                if (root!!.getChildAt(i).hasFocus()) {
                    return i
                }
            }
            return -1
        }

    private fun deleteSelectedRow() {
        val i = selectedRow
        android.util.Log.i("menu", "" + rows[i].prod!!.id + " " +
                rows[i].prod!!.name + " " + dtPkt!!.isProductsNull)
        if (i > -1) {
            if (rows[i].prod!!.id > -1 && !dtPkt!!.isProductsNull) {
                for (it in mgr?.let { dtPkt!!.getProducts(it) }!!) {
                    if (it.id == rows[i].prod!!.id) {
                        it.isSelected = false
                        break
                    }
                }
            }
            rows.removeAt(i)
            root?.removeViewAt(i)
            if (root!!.childCount > 0) {
                root!!.getChildAt(if (i > 0) i - 1 else 0).requestFocus()
            }
            calcMenu()
            dtPkt!!.setMenuNeed2Save()
        }
    }

    private fun roundDose() {
        val pos = selectedRow
        if (pos < 0) return
        //Сначала все вычисляем
        val prod: ProductInMenu = rows[pos].prod!!
        val dps = DPS(
                Sugar(user!!.s1),
                Sugar(user!!.s2),
                user!!.factors
        )
        val dsNow = Dose(sum, user!!.factors, dps)
        val prod100 = ProductW(prod)
        prod100.setWeight(prod.getWeight() + 100f)
        val doseDiff: Float = Dose(prod100, user!!.factors,
                dps).wholeDose -  //тут величина ДПС не влияет
                Dose(prod, user!!.factors, dps).wholeDose
        val frac: Float = dsNow.wholeDose -
                floor(dsNow.wholeDose.toDouble()) as Float
        val step: Float = when (user!!.round) {
            org.diacalc.android.maths.User.ROUND_1 -> 1f
            org.diacalc.android.maths.User.ROUND_05 -> 0.5f
            else -> 1f
        }
        var i = 0f
        while (i < frac) i += step
        val upDiff = floor(dsNow.wholeDose.toDouble()) as Float + i -
                dsNow.wholeDose as Float
        val downDiff: Float = dsNow.wholeDose -
                floor(dsNow.wholeDose.toDouble()) as Float - (i - step)
        val wUp = upDiff * 100 / doseDiff
        val wDown = downDiff * 100 / doseDiff
        //Потом показываем диалог с выбором
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.RoundDialogTitle))
                .setMessage(prod.name + "\n" +
                        getString(R.string.Weight) +
                        ":" + df!!.format(prod.getWeight().toDouble())
                        + " " + getString(R.string.gramm) +
                        "\n" + getString(R.string.roundTo) + " " + step)
                .setPositiveButton("+" + df0!!.format(wUp.toDouble()) + " " + getString(R.string.gramm)) { dialog, id -> //прибавляем вес
                    prod.setWeight(prod.getWeight() + wUp)
                    rows[pos].weight?.value ?: (prod.getWeight())
                    calcMenu()
                    dtPkt?.setMenuNeed2Save()
                }
                .setNeutralButton(getString(R.string.Cancel)) { dialog, id -> dialog.cancel() }
                .setNegativeButton("-" + df0!!.format(wDown.toDouble()) + " " + getString(R.string.gramm)) { dialog, id -> //Убавляем вес
                    prod.setWeight(prod.getWeight() - wDown)
                    rows[pos].weight?.value ?: (prod.getWeight())
                    calcMenu()
                    dtPkt?.setMenuNeed2Save()
                }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun getDescription(p: ProductInMenu): String {
        return when (user?.menuInfo) {
            org.diacalc.android.maths.User.PFC_INFO -> df0!!.format(p.allProt.toDouble()) +
                    "-" + df0!!.format(p.allFat.toDouble()) +
                    "-" + df0!!.format(p.allCarb.toDouble()) +
                    "-" + p.getGi()
            org.diacalc.android.maths.User.BE_INFO -> df0!!.format(p.allCarb /
                    user!!.factors.getBE(Factors.DIRECT).toDouble())
            org.diacalc.android.maths.User.CALOR_INFO -> df!!.format(p.calories.toDouble()) + " " + getString(R.string.calor)
            org.diacalc.android.maths.User.DOSE_INFO -> df0!!.format(
                    Dose(p, user!!.factors, DPS()).wholeDose.toDouble())
            else -> "==="
        }
    }

    private fun addRow(prod: ProductInMenu) {
        val right = LinearLayout(this)
        right.orientation = LinearLayout.VERTICAL
        var ltP: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        ltP.leftMargin = 5
        right.layoutParams = ltP
        ///Наименование продукта
        ltP = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val name = TextView(this)
        name.text = prod.name
        name.textSize = 18f
        right.addView(name, ltP)
        ///Описание продукта
        ltP = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val descr = TextView(this)
        descr.text = getDescription(prod)
        descr.textSize = 12f
        right.addView(descr, ltP)
        //Закончили правую сторону
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ltP = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        ltP.bottomMargin = 5
        ll.layoutParams = ltP
        ll.setBackgroundColor(0x30FFFFFF)
        ll.setOnClickListener { v ->
            for (i in 0 until root!!.childCount) {
                if (v === root!!.getChildAt(i)) {
                    root!!.getChildAt(i).requestFocus()
                }
            }
        }
        ll.setOnLongClickListener { v ->
            for (i in 0 until root!!.childCount) {
                if (v === root!!.getChildAt(i)) {
                    root!!.getChildAt(i).requestFocus()
                }
            }
            false
        }
        registerForContextMenu(ll)
        ///Поле ввода веса
        ltP = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val edit = FloatEditText(this)
        edit.setZeroes(0)
        edit.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        edit.width = 85
        edit.textSize = 18f
        edit.value = (prod.getWeight())
        edit.hint = getString(R.string.weightGr)
        edit.setOnEditorActionListener { v, actionId, event ->
            prod.setWeight(
                    (v as FloatEditText).value
            )
            calcMenu()
            dtPkt?.setMenuNeed2Save()
            false
        }
        edit.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { //Потеряли фокус, теперь надо проверить совпадение
                if (abs(prod.getWeight() - edit.value) > 0.001f) {
                    prod.setWeight(edit.value)
                    calcMenu()
                    dtPkt?.setMenuNeed2Save()
                }
            }
        }
        ll.addView(edit, ltP)
        ll.addView(right)
        val row = RowHolder()
        row.name = name
        row.descr = descr
        row.weight = edit
        row.layout = ll
        row.prod = prod
        rows.add(row)
        root?.addView(row.layout)
    }

    //Рутинные задачи
    private fun fillSugarsButton() {
        val btn: android.widget.Button = findViewById<android.view.View>(R.id.btnMenuDPS) as android.widget.Button
        val d: DecimalFormat? = if (user?.isMmol!!) df0 else df
        if (d != null) {
            btn.text = "${getString(R.string.sugar1)}=" +
                    "${d.format(Sugar(user!!.s1).getSugar(user!!.isMmol, user!!.isPlasma).toDouble())}" +
                    " ${getString(R.string.sugar2)}=" +
                    "${d.format(Sugar(user!!.s2).getSugar(user!!.isMmol, user!!.isPlasma).toDouble())}\n" +
                    "${getString(R.string.k3)}=" +
                    "${d.format(Sugar(user!!.factors.k3).getSugar(user!!.isMmol, user!!.isPlasma).toDouble())}"
        }
    }

    private fun fillButtonCoef() {
        val btn: android.widget.Button = findViewById<android.view.View>(R.id.btnMenuCoef) as android.widget.Button
        btn.text = "${getString(R.string.k1)}=" +
                "${df00?.format(user?.factors?.getK1(Factors.DIRECT)?.toDouble())} " +
                "${getString(R.string.k2)}=" +
                "${df00?.format(user?.factors?.getK2()?.toDouble())}\n" +
                "${getString(R.string.BE)}=${df?.format(user?.factors?.getBE(Factors.DIRECT)?.toDouble())}"
    }

    @SuppressLint("SetTextI18n")
    private fun calcMenu() {
        sum = ProductW()
        for (i in rows.indices) {
            rows[i].prod?.let { sum!!.plusProd(it) }
            rows[i].descr?.text = rows[i].prod?.let { getDescription(it) }
        }
        val s1 = user?.s1?.let { Sugar(it) }
        val s2 = user?.s2?.let { Sugar(it) }
        val dps = s1?.let {
            if (s2 != null) {
                DPS(it, s2, user?.factors)
            }
        }
        val ds = Dose(sum!!, user?.factors, dps) // добавил пустой конструктор в DPS
        //тут заносим БД, МД и т.д.
        textBD?.text = df0?.format(ds.getCarbFastDose() + ds.dPSDose.toDouble())
        textMD?.text = df0?.format(ds.getCarbSlowDose() + ds.getSlowDose().toDouble())
        textSumD?.text = df0?.format(ds.wholeDose.toDouble())
        when (user?.menuInfo) {
            org.diacalc.android.maths.User.PFC_INFO -> textInfo?.text =
                    "${df0!!.format(sum!!.allProt.toDouble())}-" +
                    "${df0!!.format(sum!!.allFat.toDouble())}-" +
                    "${df0!!.format(sum!!.allCarb.toDouble())}-" +
                    "${sum!!.getGi()}"
            org.diacalc.android.maths.User.BE_INFO -> textInfo?.text =
                    "" + df0!!.format(sum!!.allCarb /
                    user!!.factors.getBE(Factors.DIRECT).toDouble())
            org.diacalc.android.maths.User.DOSE_INFO,
            org.diacalc.android.maths.User.CALOR_INFO -> textInfo?.text =
                    "" + df!!.format(sum!!.calories.toDouble()) + " " + getString(R.string.calor)
        }
        //Тут меняем заголовок
/*String name = getName();
        if (fieldempty){
            name += " *";
        }
        if (Math.abs(ds.getDPSDose())>0){
            name += " Д";
        }
        if (master.getUser().isTimeSense()){
            name += " t";
        }
        form.setTitle( name );*/
    }

    /*private void getProds(){
        try{
            m_prods = new ArrayList<ProductInMenu>();
            m_prods.add(new ProductInMenu("Первый",2.5f,3.6f,7.8f,35,75f,101));
    		m_prods.add(new ProductInMenu("Второй",3.5f,4.6f,17.8f,65,35f,102));
    		m_prods.add(new ProductInMenu("Третий",4.5f,5.6f,27.8f,55,80f,103));
    		m_prods.add(new ProductInMenu("Четвертый",4.5f,5.6f,27.8f,55,80f,103));
    		m_prods.add(new ProductInMenu("Пятый",4.5f,5.6f,27.8f,55,80f,103));
    		m_prods.add(new ProductInMenu("Шестой",4.5f,5.6f,27.8f,55,80f,103));
    		m_prods.add(new ProductInMenu("Седьмой",4.5f,5.6f,27.8f,55,80f,103));
    		m_prods.add(new ProductInMenu("Восьмой",4.5f,5.6f,27.8f,55,80f,103));
            //Thread.sleep(100);
            Log.i("ARRAY", ""+ m_prods.size());
          } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          runOnUiThread(returnRes);
	}
	private Runnable returnRes = new Runnable() {
		@Override
        public void run() {
            if(m_prods != null && m_prods.size() > 0){
                m_adapter.notifyDataSetChanged();
                for(int i=0;i<m_prods.size();i++)
                	m_adapter.add(m_prods.get(i));
            }
            m_ProgressDialog.dismiss();
            m_adapter.notifyDataSetChanged();
        }
   };*/
//Класс содержащий строку в меню
    internal inner class RowHolder {
        var weight: FloatEditText? = null
        var descr: TextView? = null
        var name: TextView? = null
        var layout: LinearLayout? = null
        var prod: ProductInMenu? = null
    }

    companion object {
        private const val DIALOG_COEFS_ID = 100 + 0
        private const val DIALOG_DRS_ID = 100 + 1
    }
}