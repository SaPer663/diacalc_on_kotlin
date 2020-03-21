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
//import org.diacalc.android.internet.DoingPost
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.DPS
import org.diacalc.android.maths.Dose
import org.diacalc.android.maths.Factors
import org.diacalc.android.maths.Sugar
import org.diacalc.android.products.ProductInMenu
import org.diacalc.android.products.ProductFeatures
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.floor

class MenuForm : Activity() {

    private val rowsHolder: java.util.ArrayList<RowHolder> = java.util.ArrayList()
    private lateinit var dataPocket: DataPocket
    private lateinit var user: org.diacalc.android.maths.User
    private lateinit var listInMenuBar: LinearLayout //Панель в которой находится список
    private lateinit var textViewFastDose: TextView
    private lateinit var textViewSlowDose: TextView
    private lateinit var textViewTotalDose: TextView
    private lateinit var textViewInfo: TextView
    private lateinit var decimalFormat: DecimalFormat
    private lateinit var decimalFormat0: DecimalFormat
    private lateinit var decimalFormat00: DecimalFormat
    private lateinit var productFeatures: ProductFeatures  //Что бы не дергать каждый раз меню для округления
    private lateinit var databaseManager: DatabaseManager
    private val selectedItemInMenuBar: Int
        get() {
            for (i in 0 until listInMenuBar.childCount) {
                if (listInMenuBar.getChildAt(i).hasFocus()) {
                    return i
                }
            }
            return -1
        }
    /* Работа с диалогами
     */
    override fun onCreateDialog(id: Int): android.app.Dialog? {
        return when (id) {
            DIALOG_ENTERING_COEFFICIENT_VALUE_ID -> createEnteringCoefficientValuesDialog()
            DIALOG_ENTERING_BLOOD_SUGAR_VALUE_ID -> createEnteringBloodSugarValuesDialog()
            else -> null
        }
    }

    private fun createEnteringBloodSugarValuesDialog(): android.app.Dialog {
        val dialog: android.app.Dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.menu_drs_dlg)
        dialog.setTitle(dialog.context.getString(R.string.SugarDialogTitle))
        var zeros = 0
        if (user.isMmol) zeros = 1
        val floatEditTextHighBloodSugar: FloatEditText = dialog.findViewById(R.id.editMenuDlgS1) as FloatEditText
        floatEditTextHighBloodSugar.setZeroesAfterDecimalPoint(zeros)
        val floatEditTextBloodSugarTarget: FloatEditText = dialog.findViewById(R.id.editMenuDlgS2) as FloatEditText
        floatEditTextBloodSugarTarget.setZeroesAfterDecimalPoint(zeros)
        val floatEditTextUnitCostOfInsulin: FloatEditText = dialog.findViewById(R.id.editMenuDlgOUV) as FloatEditText
        floatEditTextUnitCostOfInsulin.setZeroesAfterDecimalPoint(zeros + 1)
        dialog.setOnDismissListener { // TODO Auto-generated method stub
            val sugar = Sugar()
            sugar.setSugar(floatEditTextUnitCostOfInsulin.formattedValue, user.isMmol, user.isPlasma)
            user.factorsProperty.unitCostOfInsulin = sugar.value
            sugar.setSugar(floatEditTextHighBloodSugar.formattedValue, user.isMmol, user.isPlasma)
            user.highBloodSugar = sugar.value
            sugar.setSugar(floatEditTextBloodSugarTarget.formattedValue, user.isMmol, user.isPlasma)
            user.bloodSugarTargets = sugar.value
            //Где то тут надо проверить, считать дальше или
//предупредить о быстром снижении сахаров
            if (user.highBloodSugar - user.bloodSugarTargets > 5f) {
                val safeBloodSugarLoweringValue: Float = user.highBloodSugar - 5f
                val outputFormat: DecimalFormat = if (user.isMmol) decimalFormat0 else decimalFormat
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MenuForm)
                builder.setMessage(getString(R.string.sugarFastAlert) + " " +
                                outputFormat.format(Sugar(safeBloodSugarLoweringValue)
                                        .getSugar(user.isMmol, user.isPlasma).toDouble())
                        )
                        .setCancelable(false)
                        .setPositiveButton(this@MenuForm.getString(R.string.btnOk)
                        ) { _, _ -> //Нажали да
                            user.bloodSugarTargets = safeBloodSugarLoweringValue
                            contentSugarsButton()
                            calcMenu()
                        }
                        .setNegativeButton(this@MenuForm.getString(R.string.btnNo),
                                null) //Кнопку нет не надо слушать
                val alert: AlertDialog = builder.create()
                alert.show()
            }
            calcMenu()
            //Надо еще значения в кнопки записать
            contentSugarsButton()
        }
        val buttonOk: android.widget.Button = dialog.findViewById(R.id.btnMenuDRSDlgOk) as android.widget.Button
        buttonOk.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    private fun createEnteringCoefficientValuesDialog(): android.app.Dialog {
        val dialog: android.app.Dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.menu_coefs_dlg)
        dialog.setTitle(getString(R.string.SugarDialogTitle))
        val floatEditTextK1: FloatEditText = dialog.findViewById(R.id.editMenuDlgK1) as FloatEditText
        floatEditTextK1.setZeroesAfterDecimalPoint(2)
        val floatEditTextK2: FloatEditText = dialog.findViewById(R.id.editMenuDlgK2) as FloatEditText
        floatEditTextK2.setZeroesAfterDecimalPoint(2)
        val floatEditTextBaseUnit: FloatEditText = dialog.findViewById(R.id.editMenuDlgBE) as FloatEditText
        floatEditTextBaseUnit.setZeroesAfterDecimalPoint(0)
        dialog.setOnDismissListener { // TODO Auto-generated method stub
            user.factorsProperty.setK1BaseUnit(floatEditTextK1.formattedValue, floatEditTextBaseUnit.formattedValue,
                    Factors.DIRECT)
            user.factorsProperty.setK2(floatEditTextK2.formattedValue)
            //пересчитываем
            calcMenu()
            //Надо еще значения в кнопки записать
            contentButtonCoef()
        }
        val buttonOk: android.widget.Button = dialog.findViewById(R.id.btnMenuCoefDlgOk) as android.widget.Button
        buttonOk.setOnClickListener { dialog.dismiss() }
        return dialog
    }

    //Подготавливаем диалоги
    override fun onPrepareDialog(id: Int, dialog: android.app.Dialog) {
        when (id) {
            DIALOG_ENTERING_COEFFICIENT_VALUE_ID -> {
                val floatEditTextK1: FloatEditText = dialog.findViewById(R.id.editMenuDlgK1) as FloatEditText
                floatEditTextK1.formattedValue = (user.factorsProperty.getK1(Factors.DIRECT))
                val floatEditTextK2: FloatEditText = dialog.findViewById(R.id.editMenuDlgK2) as FloatEditText
                floatEditTextK2.formattedValue = (user.factorsProperty.getK2())
                val floatEditTextBaseUnit: FloatEditText = dialog.findViewById(R.id.editMenuDlgBE) as FloatEditText
                floatEditTextBaseUnit.formattedValue = (user.factorsProperty.getBaseUnit(Factors.DIRECT))
            }
            DIALOG_ENTERING_BLOOD_SUGAR_VALUE_ID -> {
                val floatEditTextHighBloodSugar: FloatEditText = dialog.findViewById(R.id.editMenuDlgS1) as FloatEditText
                floatEditTextHighBloodSugar.formattedValue = (Sugar(user.highBloodSugar).getSugar(user.isMmol,
                        user.isPlasma))
                val floatEditTextBloodSugarTargets: FloatEditText = dialog.findViewById(R.id.editMenuDlgS2) as FloatEditText
                floatEditTextBloodSugarTargets.formattedValue = (Sugar(user.bloodSugarTargets).getSugar(user.isMmol,
                        user.isPlasma))
                val floatEditTextUnitCostOfInsulin: FloatEditText = dialog.findViewById(R.id.editMenuDlgOUV) as FloatEditText
                floatEditTextUnitCostOfInsulin.formattedValue = (Sugar(user.factorsProperty.unitCostOfInsulin)
                        .getSugar(user.isMmol,
                                user.isPlasma))
            }
            else -> {
            }
        }
    }

    //Показываем диалоги
    fun onClickButtonMenuCoef() {
        enterCorrectWeightValues()
        showDialog(DIALOG_ENTERING_COEFFICIENT_VALUE_ID)
    }

    fun onClickButtonMenuDRS() {
        enterCorrectWeightValues()
        showDialog(DIALOG_ENTERING_BLOOD_SUGAR_VALUE_ID)
    }

    private fun enterCorrectWeightValues() { //Проверяем, что пользователь видит то же значение, что и установлено
//в весе продукта
        for (i in rowsHolder.indices) {
            if (abs(rowsHolder[i].weight.formattedValue - rowsHolder[i].productInMenu.getWeight()) > 0.001) {
                rowsHolder[i].productInMenu.setWeight(rowsHolder[i].weight.formattedValue)
            }
        }
    }

    ///Строим основное окно
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)
        listInMenuBar = findViewById<View>(R.id.listPaneMenu) as LinearLayout
        databaseManager = DatabaseManager(this)
        dataPocket = this.application as DataPocket
        user = dataPocket.getUserFromBD(databaseManager)
        var formatUS: java.text.NumberFormat = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (formatUS is DecimalFormat) {
            decimalFormat0 = formatUS
            decimalFormat0.applyPattern("0.0")
        }
        formatUS = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (formatUS is DecimalFormat) {
            decimalFormat = formatUS
            decimalFormat.applyPattern("0")
        }
        formatUS = java.text.NumberFormat.getInstance(java.util.Locale.US)
        if (formatUS is DecimalFormat) {
            decimalFormat00 = formatUS
            decimalFormat00.applyPattern("0.00")
        }
        dataPocket.getProductMenuFromBD(databaseManager)?.let { it ->
            for (i in it.indices) {
                dataPocket.getProductMenuFromBD(databaseManager)?.let {
                    addRow(it[i])
                }
            }
        }
        textViewFastDose = findViewById<View>(R.id.textMenuBD) as TextView
        textViewSlowDose = findViewById<View>(R.id.textMenuMD) as TextView
        textViewTotalDose = findViewById<View>(R.id.textMenuSum) as TextView
        textViewInfo = findViewById<View>(R.id.textMenuInfo) as TextView
        contentButtonCoef()
        contentSugarsButton()
        calcMenu()
    }

    override fun onPause() { //Тут сохраняем данные
        super.onPause()
        if (dataPocket.isNeedToSaveMenu) {
            android.util.Log.i("Menu", "need to save")
            dataPocket.getProductMenuFromBD(databaseManager)?.clear()
            android.util.Log.i("Menu", "" + dataPocket.getProductMenuFromBD(databaseManager)?.size)
            dataPocket.getProductMenuFromBD(databaseManager)?.let {
                for (i in rowsHolder.indices) {
                    it.add(rowsHolder[i].productInMenu) }
            }
            android.util.Log.i("Menu", "" + dataPocket.getProductMenuFromBD(databaseManager)?.size)
        }
    }

    fun onStarMenuButtonClick() {
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
//                downloadMenu()   загрузка меню с сервера
                true
            }
            R.id.uploadMenu -> {
//                uploadMenu()      выгрузка меню на сервер
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


    private fun deleteOldFromMenu() {
        var renew = false
        for (i in rowsHolder.size downTo 1) {
            if (rowsHolder[i - 1].productInMenu.id == -1) {
                listInMenuBar.removeView(rowsHolder[i - 1].linearLayout)
                rowsHolder.removeAt(i - 1)
                renew = true
            }
        }
        if (renew) {
            calcMenu()
            dataPocket.setMenuNeedToSave()
        }
    }

    private fun clearMenu() {
        if (rowsHolder.isEmpty()) return
        dataPocket.setMenuNeedToSave()
        if (!dataPocket.isProductsNull) {
            dataPocket.getProductsFromBD(databaseManager)?.let {
                for (product in it) {
                    if (product.isSelected) product.isSelected = false
                }
            }
        }
        rowsHolder.clear()
        listInMenuBar.removeAllViews()
        calcMenu()
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View,
                                     menuInfo: ContextMenu.ContextMenuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_contxmenu, menu)
    }

    override fun onContextItemSelected(item: android.view.MenuItem): Boolean { //AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        return when (item.itemId) {
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



    private fun deleteSelectedRow() {
        val i = selectedItemInMenuBar
        android.util.Log.i("menu", "" + rowsHolder[i].productInMenu.id + " " +
                rowsHolder[i].productInMenu.name + " " + dataPocket.isProductsNull)
        if (i > -1) {
            if (rowsHolder[i].productInMenu.id > -1 && !dataPocket.isProductsNull) {
                dataPocket.getProductsFromBD(databaseManager)?.let {
                    for (probuctInBase in it) {
                        if (probuctInBase.id == rowsHolder[i].productInMenu.id) {
                            probuctInBase.isSelected = false
                            break
                        }
                    }
                }
            }
            rowsHolder.removeAt(i)
            listInMenuBar.removeViewAt(i)
            if (listInMenuBar.childCount > 0) {
                listInMenuBar.getChildAt(if (i > 0) i - 1 else 0).requestFocus()
            }
            calcMenu()
            dataPocket.setMenuNeedToSave()
        }
    }

    private fun roundDose() {
        val item = selectedItemInMenuBar
        if (item < 0) return
        //Сначала все вычисляем
        val product: ProductInMenu = rowsHolder[item].productInMenu
        val dps = DPS(
                Sugar(user.highBloodSugar),
                Sugar(user.bloodSugarTargets),
                user.factorsProperty
        )
        val currentDose = Dose(productFeatures, user.factorsProperty, dps)
        val productWeighing100 = ProductFeatures(product)
        productWeighing100.setWeight(product.getWeight() + 100f)
        val doseDiff: Float = Dose(productWeighing100, user.factorsProperty,
                dps).wholeDose -  //тут величина ДПС не влияет
                Dose(product, user.factorsProperty, dps).wholeDose
        val frac: Float = currentDose.wholeDose -
                floor(currentDose.wholeDose.toDouble()).toFloat()
        val step: Float = when (user.round) {
            org.diacalc.android.maths.User.ROUND_1 -> 1f
            org.diacalc.android.maths.User.ROUND_05 -> 0.5f
            else -> 1f
        }
        var i = 0f
        while (i < frac) i += step
        val upDiff = floor(currentDose.wholeDose.toDouble()).toFloat() + i -
                currentDose.wholeDose
        val downDiff: Float = currentDose.wholeDose -
                floor(currentDose.wholeDose.toDouble()).toFloat() - (i - step)
        val wUp = upDiff * 100 / doseDiff
        val wDown = downDiff * 100 / doseDiff
        //Потом показываем диалог с выбором
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.RoundDialogTitle))
                .setMessage(product.name + "\n" +
                        getString(R.string.Weight) +
                        ":" + decimalFormat.format(product.getWeight().toDouble())
                        + " " + getString(R.string.gramm) +
                        "\n" + getString(R.string.roundTo) + " " + step)
                .setPositiveButton("+" + decimalFormat0.format(wUp.toDouble()) + " " + getString(R.string.gramm)) { _, _ -> //прибавляем вес
                    product.setWeight(product.getWeight() + wUp)
                    rowsHolder[item].weight.formattedValue
                    calcMenu()
                    dataPocket.setMenuNeedToSave()
                }
                .setNeutralButton(getString(R.string.Cancel)) { dialog, _ -> dialog.cancel() }
                .setNegativeButton("-" + decimalFormat0.format(wDown.toDouble()) + " " + getString(R.string.gramm)) { _, _ -> //Убавляем вес
                    product.setWeight(product.getWeight() - wDown)
                    rowsHolder[item].weight.formattedValue
                    calcMenu()
                    dataPocket.setMenuNeedToSave()
                }
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    private fun getProductDescription(product: ProductInMenu): String {
        return when (user.menuInfo) {
            org.diacalc.android.maths.User.PFC_INFO -> decimalFormat0.format(product.allProteins.toDouble()) +
                    "-" + decimalFormat0.format(product.allFats.toDouble()) +
                    "-" + decimalFormat0.format(product.allCarbohydrates.toDouble()) +
                    "-" + product.getGi()
            org.diacalc.android.maths.User.BE_INFO -> decimalFormat0.format(product.allCarbohydrates /
                    user.factorsProperty.getBaseUnit(Factors.DIRECT).toDouble())
            org.diacalc.android.maths.User.CALOR_INFO -> decimalFormat.format(product.calories.toDouble()) + " " + getString(R.string.calor)
            org.diacalc.android.maths.User.DOSE_INFO -> decimalFormat0.format(
                    Dose(product, user.factorsProperty, DPS()).wholeDose.toDouble())
            else -> "==="
        }
    }

    private fun addRow(product: ProductInMenu) {
        val rightSideOfScreen = LinearLayout(this)
        rightSideOfScreen.orientation = LinearLayout.VERTICAL
        var lineaLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        lineaLayoutParams.leftMargin = 5
        rightSideOfScreen.layoutParams = lineaLayoutParams
        ///Наименование продукта
        lineaLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val nameProduct = TextView(this)
        nameProduct.text = product.name
        nameProduct.textSize = 18f
        rightSideOfScreen.addView(nameProduct, lineaLayoutParams)
        ///Описание продукта
        lineaLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val productDescriptions = TextView(this)
        productDescriptions.text = getProductDescription(product)
        productDescriptions.textSize = 12f
        rightSideOfScreen.addView(productDescriptions, lineaLayoutParams)
        //Закончили правую сторону
        val mainPartOfScreen = LinearLayout(this)
        mainPartOfScreen.orientation = LinearLayout.HORIZONTAL
        lineaLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        lineaLayoutParams.bottomMargin = 5
        mainPartOfScreen.layoutParams = lineaLayoutParams
        mainPartOfScreen.setBackgroundColor(0x30FFFFFF)
        mainPartOfScreen.setOnClickListener { v ->
            for (i in 0 until listInMenuBar.childCount) {
                if (v === listInMenuBar.getChildAt(i)) {
                    listInMenuBar.getChildAt(i).requestFocus()
                }
            }
        }
        mainPartOfScreen.setOnLongClickListener { v ->
            for (i in 0 until listInMenuBar.childCount) {
                if (v === listInMenuBar.getChildAt(i)) {
                    listInMenuBar.getChildAt(i).requestFocus()
                }
            }
            false
        }
        registerForContextMenu(mainPartOfScreen)
        ///Поле ввода веса
        lineaLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        val weightEntryField = FloatEditText(this)
        weightEntryField.setZeroesAfterDecimalPoint(0)
        weightEntryField.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        weightEntryField.width = 85
        weightEntryField.textSize = 18f
        weightEntryField.formattedValue = (product.getWeight())
        weightEntryField.hint = getString(R.string.weightGr)
        weightEntryField.setOnEditorActionListener { value, _, _ ->
            product.setWeight(
                    (value as FloatEditText).formattedValue
            )
            calcMenu()
            dataPocket.setMenuNeedToSave()
            false
        }
        weightEntryField.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { //Потеряли фокус, теперь надо проверить совпадение
                if (abs(product.getWeight() - weightEntryField.formattedValue) > 0.001f) {
                    product.setWeight(weightEntryField.formattedValue)
                    calcMenu()
                    dataPocket.setMenuNeedToSave()
                }
            }
        }
        mainPartOfScreen.addView(weightEntryField, lineaLayoutParams)
        mainPartOfScreen.addView(rightSideOfScreen)
        val row = RowHolder()
        row.name = nameProduct
        row.description = productDescriptions
        row.weight = weightEntryField
        row.linearLayout = mainPartOfScreen
        row.productInMenu = product
        rowsHolder.add(row)
        listInMenuBar.addView(row.linearLayout)
    }

    //Рутинные задачи
    private fun contentSugarsButton() {
        val btn: android.widget.Button = findViewById<View>(R.id.btnMenuDPS) as android.widget.Button
        val d: DecimalFormat? = if (user.isMmol) decimalFormat0 else decimalFormat
        if (d != null) {
            btn.text = "${getString(R.string.sugar1)}=" +
                    "${d.format(Sugar(user.highBloodSugar).getSugar(user.isMmol, user.isPlasma).toDouble())}" +
                    " ${getString(R.string.sugar2)}=" +
                    "${d.format(Sugar(user.bloodSugarTargets).getSugar(user.isMmol, user.isPlasma).toDouble())}\n" +
                    "${getString(R.string.k3)}=" +
                    "${d.format(Sugar(user.factorsProperty.unitCostOfInsulin).getSugar(user.isMmol, user.isPlasma).toDouble())}"
        }
    }

    private fun contentButtonCoef() {
        val btn: android.widget.Button = findViewById<View>(R.id.btnMenuCoef) as android.widget.Button
        btn.text = "${getString(R.string.k1)}=" +
                "${decimalFormat00.format(user.factorsProperty.getK1(Factors.DIRECT).toDouble())} " +
                "${getString(R.string.k2)}=" +
                "${decimalFormat00.format(user.factorsProperty.getK2().toDouble())}\n" +
                "${getString(R.string.BE)}=${decimalFormat.format(user.factorsProperty.getBaseUnit(Factors.DIRECT).toDouble())}"
    }

    @SuppressLint("SetTextI18n")
    private fun calcMenu() {
        productFeatures = ProductFeatures()
        for (i in rowsHolder.indices) {
            productFeatures.productAddition(rowsHolder[i].productInMenu)
            rowsHolder[i].description.text = getProductDescription(rowsHolder[i].productInMenu)
        }
        val highBloodSugar = Sugar(user.highBloodSugar)
        val bloodSugarTargets = Sugar(user.bloodSugarTargets)
        val dps = DPS(highBloodSugar, bloodSugarTargets, user.factorsProperty)
        val dose = Dose(productFeatures, user.factorsProperty, dps) // добавил пустой конструктор в DPS
        //тут заносим БД, МД и т.д.
        textViewFastDose.text = decimalFormat0.format(dose.getCarbohydratesFastDose() + dose.dPSDose)
        textViewSlowDose.text = decimalFormat0.format(dose.getCarbohydratesSlowDose() + dose.getSlowDose())
        textViewTotalDose.text = decimalFormat0.format(dose.wholeDose.toDouble())
        when (user.menuInfo) {
            org.diacalc.android.maths.User.PFC_INFO -> textViewInfo.text =
                    "${decimalFormat0.format(productFeatures.allProteins.toDouble())}-" +
                            "${decimalFormat0.format(productFeatures.allFats.toDouble())}-" +
                            "${decimalFormat0.format(productFeatures.allCarbohydrates.toDouble())}-" +
                            "${productFeatures.getGi()}"
            org.diacalc.android.maths.User.BE_INFO -> textViewInfo.text =
                    "" + decimalFormat0.format(productFeatures.allCarbohydrates /
                            user.factorsProperty.getBaseUnit(Factors.DIRECT).toDouble())
            org.diacalc.android.maths.User.DOSE_INFO,
            org.diacalc.android.maths.User.CALOR_INFO -> textViewInfo.text =
                    "" + decimalFormat.format(productFeatures.calories.toDouble()) + " " + getString(R.string.calor)
        }
    }
        //Тут меняем заголовок

//Класс содержащий строку в меню
    internal inner class RowHolder {
        lateinit var weight: FloatEditText
        lateinit var description: TextView
        lateinit var name: TextView
        lateinit var linearLayout: LinearLayout
        lateinit var productInMenu: ProductInMenu
    }

    companion object {
        private const val DIALOG_ENTERING_COEFFICIENT_VALUE_ID = 100 + 0
        private const val DIALOG_ENTERING_BLOOD_SUGAR_VALUE_ID = 100 + 1
    }
}

/*
    private fun uploadMenu() {
        val uploadingMenu: Thread = object : Thread() {
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
*/

/*
    private fun downloadMenu() {
        val loadingMenu: Thread = object : Thread() {
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
                                .setMessage(getString(R.string.menuLoadedMsg) +
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
*/
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
