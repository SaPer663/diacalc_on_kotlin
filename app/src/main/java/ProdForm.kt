package org.diacalc.android

import android.app.AlertDialog
import android.app.Dialog
import android.app.ListActivity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView.OnItemClickListener
import org.diacalc.android.components.FloatEditText
import org.diacalc.android.internet.DoingPost
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import org.diacalc.android.products.ProductW
import java.util.*


class ProdForm : ListActivity() {
    private var groupTitle: TextView? = null
    private var groupSelected = -1
    private var prodSelected = -1
    private var adapter: ProductProxyAdapter? = null
    private lateinit var groups: ArrayList<ProductGroup>
    private var prods: ArrayList<ProductInBase>? = null
    private var mgr: DatabaseManager? = null
    private var user: User? = null
    private var dtPkt: DataPocket? = null

    //для использования в отдельном потоке
    private var iProds: ArrayList<ProductInBase>? = null
    private var iGroup: ArrayList<ProductGroup>? = null
    private var iMsg = ""
    private var iProgressdialog: ProgressDialog? = null
    private val searchMode = false
    private var menuProds: ArrayList<ProductInMenu>? = null
    private val prodsComparator = Comparator<ProductW> { p1, p2 -> p1.name.compareTo(p2.name) }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prods)
        mgr = DatabaseManager(this)
        dtPkt = this.application as DataPocket
        user = dtPkt!!.getUser(mgr!!)
        groupTitle = findViewById<View>(R.id.textProdGroupName) as TextView
        groups = dtPkt!!.getGroups(mgr!!)!!
        prods = dtPkt!!.getProducts(mgr!!)
        menuProds = dtPkt!!.getMenuProds(mgr!!)
        adapter = ProductProxyAdapter(this,
                dtPkt!!.getProducts(mgr!!))
        if (savedInstanceState != null) {
            groupSelected = savedInstanceState.getInt("selectedGroup")
            prodSelected = savedInstanceState.getInt("selectedRow")
            adapter!!.filter(groups!![groupSelected].id)
        } else if (groups!!.isNotEmpty()) {
            groupSelected = 0
            adapter!!.filter(groups!![groupSelected].id)
        }
        setGroupName(groupSelected)
        listView.adapter = adapter
        if (prodSelected > -1) listView.setSelection(prodSelected)

        //getListView().setTextFilterEnabled(true);
        listView.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val pr = adapter!!.getItem(position) as ProductInBase
            if (pr.isSelected) { //Убираем его из меню
                for (i in menuProds!!.indices) {
                    if (menuProds!![i].id == pr.id) {
                        menuProds!!.removeAt(i)
                        break
                    }
                }
            } else {
                menuProds!!.add(ProductInMenu(pr))
                //тут нужна сортировка
                Collections.sort(menuProds, prodsComparator)
            }
            dtPkt!!.setMenuNeed2Save()
            pr.isSelected = !pr.isSelected
            adapter!!.notifyDataSetChanged()
        }
        registerForContextMenu(listView)

        // Get the intent, verify the action and get the query
        /*Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      Log.i("searching",query);
	      //doMySearch(query);
	    }*/
    }

    fun onStarButtonClick(v: View?) {
        val intent = Intent()
        intent.setClass(baseContext, MenuForm::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putInt("selectedGroup", groupSelected)
        savedInstanceState.putInt("selectedRow", listView.selectedItemPosition)
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun setGroupName(sel: Int) {
        if (groups.isEmpty()) {
            groupSelected = -1
            groupTitle?.text = getString(R.string.noGroups)
        } else {
            groupSelected = sel
            groupTitle?.text = "${groupSelected + 1}. ${groups[groupSelected].name}"
        }
    }

    fun onClickMoveLeft() {
        if (searchMode || groups.isEmpty()) return
        if (groupSelected > 0) groupSelected-- else groupSelected = groups.size - 1
        setGroupName(groupSelected)
        adapter?.filter(groups[groupSelected].id)
    }

    fun onClickMoveRight() {
        if (searchMode || groups.isEmpty()) return
        if (groupSelected < groups.size - 1) groupSelected++ else groupSelected = 0
        setGroupName(groupSelected)
        adapter?.filter(groups[groupSelected].id)
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
                downloadProducts()
                true
            }
            R.id.createProductSub -> {
                showDialog(DIALOG_PROD_NEW_ID)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                     menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        if (v === listView) {
            inflater.inflate(R.menu.prods_context, menu)
            val info = menuInfo as AdapterContextMenuInfo
            menu.setHeaderTitle(
                    (adapter?.getItem(info.position) as ProductInBase).name
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

    private var productWasSelected = -1
    private fun showDialog(id: Int, pos: Int) {
        productWasSelected = pos
        showDialog(id)
        productWasSelected = -1
    }

    private fun deleteProduct(pos: Int) {
        val prod = adapter!!.getItem(pos) as ProductInBase
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.Delete))
                .setMessage("""
    ${getString(R.string.deleteProduct)}:
    ${prod.name}
    """.trimIndent())
                .setPositiveButton(getString(R.string.btnOk)) { dialog, id ->
                    prods?.remove(prod)
                    mgr?.deleteProduct(prod)
                    adapter?.filter(groups[groupSelected].id)
                }
                .setNegativeButton(getString(R.string.btnNo)) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

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
            DIALOG_PROD_EDIT_ID -> prepareProductDialog(dialog, adapter!!.getItem(productWasSelected) as ProductInBase)
        }
    }

    private fun prepareProductDialog(dialog: Dialog, prod: ProductInBase?) {
        val sp = dialog.findViewById<View>(R.id.spinnerGroupSelectProdDlg) as Spinner
        val adapter = ArrayAdapter(
                this@ProdForm,
                android.R.layout.simple_spinner_item,
                groups)
        sp.adapter = adapter
        if (prod == null) {
            dialog.setTitle(getString(R.string.newProduct))
            (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                    .setText("")
            (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText).value = 100f
            (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText).value = 0f
            (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText).value = 0f
            (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText).value = 0f
            (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText).value = 50f
            sp.setSelection(groupSelected)
        } else {
            dialog.setTitle(getString(R.string.editProduct))
            (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                    .setText(prod.name)
            (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText).value = prod.weight
            (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText).value = prod.allProt
            (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText).value = prod.allFat
            (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText).value = prod.allCarb
            (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText).value = prod.gi.toFloat()
            var p = 0
            for (i in groups!!.indices) {
                if (prod.owner == groups!![i].id) {
                    p = i
                    break
                }
            }
            sp.setSelection(p)
        }
        val btnOk = dialog.findViewById<View>(R.id.btnProdDlgOk) as Button
        btnOk.setOnClickListener(null)
        btnOk.setOnClickListener { // А тут делаем некие действия по созданию продукта
            groupSelected = sp.selectedItemPosition
            val grId = groups!![groupSelected].id
            if (prod == null) { //Создаем новый
                val w = (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText)
                        .value
                val pr = ProductInBase(
                        (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                                .text.toString(),
                        100f * (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText)
                                .value / w,
                        100f * (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText)
                                .value / w,
                        100f * (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText)
                                .value / w,
                        (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                                .value.toInt(),
                        w,
                        true,  //mobile
                        grId,  //Владелец
                        0,
                        -1
                )
                pr.weight = 100f
                mgr!!.insertProduct(pr)
                prods!!.add(pr)
            } else {
                val w = (dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText)
                        .value
                prod.name = (dialog.findViewById<View>(R.id.editNameProdDlg) as EditText)
                        .text.toString()
                prod.prot = 100f * (dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText)
                        .value / w
                prod.fat = 100f * (dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText)
                        .value / w
                prod.carb = 100f * (dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText)
                        .value / w
                prod.gi = (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                        .value.toInt()
                prod.weight = w
                prod.isMobile = true
                prod.owner = grId
                prod.weight = 100f
                mgr!!.changeProduct(prod)
            }
            dialog.dismiss()
            Collections.sort(prods, prodsComparator)
            setGroupName(groupSelected)
            this@ProdForm.adapter!!.changeProds(prods, groups!![groupSelected].id)
        }
        val btnNo = dialog.findViewById<View>(R.id.btnProdDlgNo) as Button
        btnNo.setOnClickListener(null)
        btnNo.setOnClickListener { //Тут просто закрываем
            dialog.dismiss()
        }
    }

    private fun createProdDlg(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.product_dialog)
        (dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText)
                .setZeroes(0)
        val name = dialog.findViewById<View>(R.id.editNameProdDlg) as EditText
        val weight = dialog.findViewById<View>(R.id.editWeightProdDlg) as FloatEditText
        val prot = dialog.findViewById<View>(R.id.editProtProdDlg) as FloatEditText
        val fat = dialog.findViewById<View>(R.id.editFatProdDlg) as FloatEditText
        val carb = dialog.findViewById<View>(R.id.editCarbProdDlg) as FloatEditText
        val gi = dialog.findViewById<View>(R.id.editGiProdDlg) as FloatEditText
        name.nextFocusDownId = R.id.editWeightProdDlg
        weight.nextFocusDownId = R.id.editProtProdDlg
        prot.nextFocusDownId = R.id.editFatProdDlg
        fat.nextFocusDownId = R.id.editCarbProdDlg
        carb.nextFocusDownId = R.id.editGiProdDlg
        gi.nextFocusDownId = R.id.spinnerGroupSelectProdDlg
        return dialog
    }

    private inner class ProductProxyAdapter(c: Context?, prods: ArrayList<ProductInBase>?) : BaseAdapter() {
        private var filter: ArrayList<Int>? = null
        private var underlying //неотфильтрованные продукты
                : ArrayList<ProductInBase>?
        private val mInflater: LayoutInflater = LayoutInflater.from(c)
        fun changeProds(prods: ArrayList<ProductInBase>?, owner: Int) {
            underlying = prods
            filter(owner)
        }

        private fun getFilterOffset(index: Int): Int {
            if (filter == null) {
                return index
            }
            return if (filter!!.size > index && index >= 0) {
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
            for (iter in underlying!!.indices) {
                if (underlying!![iter].owner == owner
                        || owner == -1) {
                    filter!!.add(iter)
                }
            }
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            // TODO Auto-generated method stub
            /*if (underlying!=null) return underlying.size();
			else return 0;*/
            return if (filter != null) filter!!.size else 0
        }

        override fun getItem(index: Int): ProductInBase {
            return (if (index < 0) null else underlying!![getFilterOffset(index)]) as ProductInBase
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            var convertView = convertView
            val holder: ViewHolder

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.product_item, null)

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = ViewHolder()
                holder.name = convertView.findViewById<View>(R.id.textProductName) as TextView
                holder.descr = convertView.findViewById<View>(R.id.textProductDescription) as TextView
                holder.selected = convertView.findViewById<View>(R.id.checkProductSelected) as CheckBox
                convertView.tag = holder
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = convertView.tag as ViewHolder
            }

            // Bind the data efficiently with the holder.
            holder.name!!.text = (getItem(position) as ProductInBase).name
            holder.descr!!.text = """${underlying!![getFilterOffset(position)].prot}-${underlying!![getFilterOffset(position)].fat}-${underlying!![getFilterOffset(position)].carb}-${underlying!![getFilterOffset(position)].gi}"""
            holder.selected!!.isChecked = underlying!![getFilterOffset(position)].isSelected
            return convertView
        }

        internal inner class ViewHolder {
            var name: TextView? = null
            var descr: TextView? = null
            var selected: CheckBox? = null
        }

        init {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            underlying = prods
        }
    }

    companion object {
        private const val DIALOG_PROD_NEW_ID = 200 + 0
        private const val DIALOG_PROD_EDIT_ID = 200 + 1
        private const val DIALOG_GROUP_ID = 200 + 2
    }
}