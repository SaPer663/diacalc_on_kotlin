package org.diacalc.android

import android.app.AlertDialog
import android.app.Dialog
import android.app.ListActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import org.diacalc.android.components.FloatEditText
//import org.diacalc.android.internet.DoingPost
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import org.diacalc.android.products.ProductFeatures
import java.util.*


class ProdForm : ListActivity() {
    private lateinit var productsGroupTitle: TextView
    private var productsGroupSelected = -1
    private var productSelected = -1
    private lateinit var adapter: ProductProxyAdapter
    private var productsGroup: ArrayList<ProductGroup>? = null
    private var products: ArrayList<ProductInBase>? = null
    private lateinit var databaseManager: DatabaseManager
    private lateinit var user: User
    private lateinit var dataPocket: DataPocket
    private var productWasSelected = -1
    private val searchMode = false
    private var menuProds: ArrayList<ProductInMenu>? = null
    private val productsComparator = Comparator<ProductFeatures> { p1, p2 -> p1.name.compareTo(p2.name) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prods)
        databaseManager = DatabaseManager(this)
        dataPocket = this.application as DataPocket
        user = dataPocket.getUserFromBD(databaseManager)
        productsGroupTitle = findViewById<View>(R.id.textProdGroupName) as TextView
        productsGroup = dataPocket.getGroupsFromBD(databaseManager)
        products = dataPocket.getProductsFromBD(databaseManager)
        menuProds = dataPocket.getProductMenuFromBD(databaseManager)
        adapter = ProductProxyAdapter(this,
                dataPocket.getProductsFromBD(databaseManager))
        if (savedInstanceState != null) {
            productsGroupSelected = savedInstanceState.getInt("selectedGroup")
            productSelected = savedInstanceState.getInt("selectedRow")
            productsGroup?.let {
            adapter.filter(it[productsGroupSelected].id) }
        } else
            productsGroup?.let {
                if (it.isNotEmpty()) {
                    productsGroupSelected = 0
                    productsGroup?.let {
                        adapter.filter(it[productsGroupSelected].id) }
                }
            }
        setGroupName(productsGroupSelected)
        listView.adapter = adapter
        if (productSelected > -1) listView.setSelection(productSelected)

        //getListView().setTextFilterEnabled(true);
        listView.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            val productInBase = adapter.getItem(position)
            if (productInBase.isSelected) { //Убираем его из меню
                menuProds?.let {
                    for (i in it.indices) {
                        if (it[i].id == productInBase.id) {
                            it.removeAt(i)
                            break
                        }
                    }
                }
            } else {
                menuProds?.let {
                    it.add(ProductInMenu(productInBase))
                    //тут нужна сортировка
                    Collections.sort(it, productsComparator)
                }
            }
            dataPocket.setMenuNeedToSave()
            productInBase.isSelected = !productInBase.isSelected
            adapter.notifyDataSetChanged()
        }
        registerForContextMenu(listView)


    }

    fun onStarButtonClick(v: View) {
        val intent = Intent()
        intent.setClass(baseContext, MenuForm::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("selectedGroup", productsGroupSelected)
        savedInstanceState.putInt("selectedRow", listView.selectedItemPosition)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun setGroupName(sel: Int) {
        productsGroup?.let {
            if (it.isEmpty()) {
                productsGroupSelected = -1
                productsGroupTitle.text = getString(R.string.noGroups)
            } else {
                productsGroupSelected = sel
                productsGroupTitle.text = "${productsGroupSelected + 1}. ${it[productsGroupSelected].name}"
            }
        }
    }

    fun onClickMoveLeft(v: View) {
        productsGroup?.let {
            if (searchMode || it.isEmpty()) return
            if (productsGroupSelected > 0) productsGroupSelected-- else productsGroupSelected = it.size - 1
            setGroupName(productsGroupSelected)
            adapter.filter(it[productsGroupSelected].id)
        }
    }

    fun onClickMoveRight(v: View) {
        productsGroup?.let {
            if (searchMode || it.isEmpty()) return
            if (productsGroupSelected < it.size - 1) productsGroupSelected++ else productsGroupSelected = 0
            setGroupName(productsGroupSelected)
            adapter.filter(it[productsGroupSelected].id)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.prods, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Обработка меню
        return when (item.itemId) {
            R.id.downloadProducts -> {
//                downloadProducts()       функция загрузки продуктов с сервера
                true
            }
            R.id.createProductSub -> {
                showDialog(DIALOG_PROD_NEW_ID)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View,
                                     menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo)
        val inflater = menuInflater
        if (view === listView) {
            inflater.inflate(R.menu.prods_context, menu)
            val info = menuInfo as AdapterContextMenuInfo
            menu.setHeaderTitle(
                    adapter.getItem(info.position).name
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.deleteProductProd -> {
                deleteProduct(info.position)
                true
            }
            R.id.createNewProductProd -> {
                showDialog(DIALOG_PROD_NEW_ID)
                true
            }
            R.id.changeProductProd -> {
                showDialog(DIALOG_PROD_EDIT_ID, info.position)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }


    private fun showDialog(id: Int, pos: Int) {
        productWasSelected = pos
        showDialog(id)
        productWasSelected = -1
    }

    private fun deleteProduct(pos: Int) {
        val prod = adapter.getItem(pos)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.Delete))
                .setMessage("""
    ${getString(R.string.deleteProduct)}:
    ${prod.name}
    """.trimIndent())
                .setPositiveButton(getString(R.string.btnOk)) { _, _ ->
                    products?.remove(prod)
                    databaseManager.deleteProduct(prod)
                    productsGroup?.let {
                        adapter.filter(it[productsGroupSelected].id) }
                }
                .setNegativeButton(getString(R.string.btnNo)) { dialog, _ -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }
/*
    private fun downloadProducts() {
        val t: Thread = object : Thread() {
            override fun run() {
                iProds = null
                iGroup = null
                iMsg = ""
                val iAnswer = DoingPost(user).requestProducts()
                if (iAnswer?.get(DoingPost.ERROR) != null) {
                    //Значит ошибка
                    iMsg = ((iAnswer[DoingPost.ERROR] as String?).toString())
                } else {
                    iProds = iAnswer?.get(DoingPost.PRODS_PRODS) as ArrayList<ProductInBase>?
                    iGroup = iAnswer?.get(DoingPost.PRODS_GROUP) as ArrayList<ProductGroup>?
                    //И вот тут надо сохранить в БД, т.к. это займет время
                    mgr?.putProducts(iGroup, iProds)
                    dtPkt?.setGroupProds2Null()
                    groups = dtPkt!!.getGroups(mgr!!)!!
                    prods = dtPkt!!.getProducts(mgr!!)
                    dtPkt?.setProdsNeed2Save()
                }
                runOnUiThread {
                    iProgressdialog!!.dismiss()
                    val builder = AlertDialog.Builder(this@ProdForm)
                    if (iMsg.isNotEmpty()) { //Выводим сообщение о ошибке
                        builder.setTitle(getString(R.string.errorTitle))
                                .setMessage("""
                                           ${getString(R.string.errorMsgProdsLoad)}
                                           $iMsg
                                           """.trimIndent())
                                .setNeutralButton(getString(R.string.btnOk)) { dialog, id -> dialog.cancel() }
                    } else {
                        builder.setTitle(getString(R.string.prodsLoadedTitle)).setMessage(getString(R.string.prodsLoadedMsg))
                                .setNeutralButton(getString(R.string.btnOk)) { dialog, id -> //тут заполняем продуктами
                                    var owner = -1
                                    setGroupName(0)
                                    if (groupSelected > -1) {
                                        owner = groups!![groupSelected].id
                                    }
                                    adapter!!.changeProds(prods, owner)
                                }
                    }
                    val alert = builder.create()
                    alert.show()
                }
            }
        }
        iProgressdialog = ProgressDialog.show(this,
                getString(R.string.pleaseWait),
                getString(R.string.prodsLoading), true)
        t.start()
    }
*/

    /* Работа с диалогами
     */
    public override fun onCreateDialog(id: Int): Dialog? {
        return when (id) {
            DIALOG_PROD_NEW_ID, DIALOG_PROD_EDIT_ID -> createProdDlg()
            else -> null
        }
    }

    //Подготавливаем диалоги
    public override fun onPrepareDialog(id: Int, dialog: Dialog) {
        when (id) {
            DIALOG_PROD_NEW_ID -> prepareProductDialog(dialog, null)
            DIALOG_PROD_EDIT_ID -> prepareProductDialog(dialog, adapter.getItem(productWasSelected) as ProductInBase)
        }
    }

    private fun prepareProductDialog(dialog: Dialog, prod: ProductInBase?) {
        val spinner = dialog.findViewById<View>(R.id.spinnerGroupSelectProdDlg) as Spinner
        val adapter: ArrayAdapter<ProductGroup> = ArrayAdapter(
                this@ProdForm,
                android.R.layout.simple_spinner_item,
                productsGroup as MutableList<ProductGroup>)
        Log.d("ProF", "$adapter")
        Log.d("ProF", "$productsGroup")
        spinner.adapter = adapter
        if (prod == null) {
            dialog.setTitle(getString(R.string.newProduct))
            (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                    .setText("")
            (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText).formattedValue = 100f
            (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText).formattedValue = 0f
            (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText).formattedValue = 0f
            (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText).formattedValue = 0f
            (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText).formattedValue = 50f
            spinner.setSelection(productsGroupSelected)
        } else {
            dialog.setTitle(getString(R.string.editProduct))
            (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                    .setText(prod.name)
            (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText).formattedValue = prod.weight
            (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText).formattedValue = prod.allProteins
            (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText).formattedValue = prod.allFats
            (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText).formattedValue = prod.allCarbohydrates
            (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText).formattedValue = prod.gi
            var p = 0
            productsGroup?.let {
                for (i in it.indices) {
                    if (prod.owner == it[i].id) {
                        p = i
                        break
                    }
                }
            }
            spinner.setSelection(p)
        }
        val btnOk = dialog.findViewById<View>(R.id.btnProdDlgOk) as Button
        btnOk.setOnClickListener(null)
        btnOk.setOnClickListener { // А тут делаем некие действия по созданию продукта
            productsGroupSelected = spinner.selectedItemPosition
                val groupsId = productsGroup?.let { it[productsGroupSelected].id } as Int
            if (prod == null) { //Создаем новый
                val weight = (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText)
                        .formattedValue
                val productInBase = ProductInBase(
                        (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                                .text.toString(),
                        100f * (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText)
                                .formattedValue / weight,
                        100f * (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText)
                                .formattedValue / weight,
                        100f * (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText)
                                .formattedValue / weight,
                        (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                                .formattedValue,
                        weight,
                        true,  //mobile
                        groupsId,  //Владелец
                        0,
                        -1
                )
                productInBase.weight = 100f
                databaseManager.insertProduct(productInBase)
                products?.add(productInBase)
            } else {
                val weight = (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText)
                        .formattedValue
                prod.name = (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                        .text.toString()
                prod.proteins = 100f * (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText)
                        .formattedValue / weight
                prod.fats = 100f * (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText)
                        .formattedValue / weight
                prod.carbohydrates = 100f * (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText)
                        .formattedValue / weight
                prod.gi = (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                        .formattedValue
                prod.weight = weight
                prod.isMobile = true
                prod.owner = groupsId
                prod.weight = 100f
                databaseManager.changeProduct(prod)
            }
            dialog.dismiss()
            Collections.sort(products as MutableList<ProductInBase>, productsComparator)
            setGroupName(productsGroupSelected)
            productsGroup?.let {
                this@ProdForm.adapter.changeProducts(products as ArrayList<ProductInBase>, it[productsGroupSelected].id) }
        }
        val buttonNo = dialog.findViewById<View>(R.id.btnProdDlgNo) as Button
        buttonNo.setOnClickListener(null)
        buttonNo.setOnClickListener { //Тут просто закрываем
            dialog.dismiss()
        }
    }

    private fun createProdDlg(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.product_dialog)
        (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                .setZeroesAfterDecimalPoint(0)
        val name = dialog.findViewById<View>(R.id.editNameProdDlg) as EditText
        val weight = dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText
        val proteins = dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText
        val fats = dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText
        val carbs = dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText
        val gi = dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText
        name.nextFocusDownId = R.id.editWeightProdDlg
        weight.nextFocusDownId = R.id.editProtProdDlg
        proteins.nextFocusDownId = R.id.editFatProdDlg
        fats.nextFocusDownId = R.id.editCarbProdDlg
        carbs.nextFocusDownId = R.id.editGiProdDlg
        gi.nextFocusDownId = R.id.spinnerGroupSelectProdDlg
        return dialog
    }

    private inner class ProductProxyAdapter(c: Context?, products: ArrayList<ProductInBase>?) : BaseAdapter() {
        private var filter: ArrayList<Int>? = null
        private var underlying: ArrayList<ProductInBase> //неотфильтрованные продукты
        private val mInflater: LayoutInflater = LayoutInflater.from(c)
        fun changeProducts(products: ArrayList<ProductInBase>, owner: Int) {
            underlying = products
            filter(owner)
        }

        private fun getFilterOffset(index: Int): Int {
            if (filter == null) {
                return index
            }
            return if (filter!!.size > index && index >= 0 ) {
                filter!![index]
            } else -1
        }

        private fun getUnderlyingOffset(index: Int): Int {
            return if (filter == null) {
                index
            } else filter!!.indexOf(index)
        }

        fun filter(owner: Int) {
            filter = ArrayList()
            for (iter in underlying.indices) {
                if (underlying[iter].owner == owner
                        || owner == -1) {
                    filter?.add(iter)
                }
            }
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            // TODO Auto-generated method stub
            /*if (underlying!=null) return underlying.size();
			else return 0;*/
            return filter?.size ?: 0
        }

        override fun getItem(index: Int): ProductInBase {
            return (if (index < 0) null else underlying[getFilterOffset(index)]) as ProductInBase
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, _convertView: View?, parent: ViewGroup): View {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            var convertView = _convertView
            val holder: ViewHolder

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.product_item, parent,false)

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = ViewHolder()
                holder.name = convertView.findViewById<View>(R.id.textProductName) as TextView
                holder.description = convertView.findViewById<View>(R.id.textProductDescription) as TextView
                holder.selected = convertView.findViewById<View>(R.id.checkProductSelected) as CheckBox
                convertView.tag = holder
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = convertView.tag as ViewHolder
            }

            // Bind the data efficiently with the holder.
            holder.name.text = getItem(position).name
            holder.description.text = """${underlying[getFilterOffset(position)].proteins}-${underlying[getFilterOffset(position)].fats}-${underlying[getFilterOffset(position)].carbohydrates}-${underlying[getFilterOffset(position)].gi}"""
            holder.selected.isChecked = underlying[getFilterOffset(position)].isSelected
            return convertView as View
        }

        internal inner class ViewHolder {
            lateinit var name: TextView
            lateinit var description: TextView
            lateinit var selected: CheckBox
        }

        init {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            underlying = products as ArrayList<ProductInBase>
        }
    }

    companion object {
        private const val DIALOG_PROD_NEW_ID = 200 + 0
        private const val DIALOG_PROD_EDIT_ID = 200 + 1
        private const val DIALOG_GROUP_ID = 200 + 2
    }
}

// Get the intent, verify the action and get the query
/*Intent intent = getIntent();
if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
  String query = intent.getStringExtra(SearchManager.QUERY);
  Log.i("searching",query);
  //doMySearch(query);
}*/
