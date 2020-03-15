package org.diacalc.android

import android.app.Application
import android.util.Log
import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import java.util.*

class DataPocket : Application() {
    private var user: User? = null
    private var menuProds: ArrayList<ProductInMenu>? = null
    var isNeed2SaveMenu = false
        private set
    private var groups: ArrayList<ProductGroup>? = null
    private var prods: ArrayList<ProductInBase>? = null
    private var need2saveProds = false
    fun setProdsNeed2Save() {
        need2saveProds = true
    }

    fun setMenuNeed2Save() {
        isNeed2SaveMenu = true
    }

    fun getProducts(mgr: DatabaseManager): ArrayList<ProductInBase>? {
        if (prods == null) { //Запросили продукты первый раз, берем из БД
            prods = mgr.products
        }
        return prods
    }

    fun getGroups(mgr: DatabaseManager): ArrayList<ProductGroup>? {
        if (groups == null) { //Запросили продукты первый раз, берем из БД
            groups = mgr.groups
        }
        return groups
    }

    fun getMenuProds(mgr: DatabaseManager): ArrayList<ProductInMenu>? {
        if (menuProds == null) { //Запросили продукты первый раз, берем из БД
            menuProds = mgr.menuProducts
        }
        return menuProds
    }

    fun setAllPointers2Null() {
        menuProds = null
        user = null
    }

    fun setGroupProds2Null() {
        prods = null
        groups = null
    }

    val isProductsNull: Boolean
        get() = prods == null

    fun storeMenuProds(mgr: DatabaseManager) {
        Log.i("DataPocket", "storing menu1 $isNeed2SaveMenu")
        if (isNeed2SaveMenu) {
            Log.i("DataPocket", "storing menu")
            if (menuProds != null) mgr.putMenuProds(menuProds)
        }
    }

    fun getUser(mgr: DatabaseManager): User? {
        if (user == null) {
            Log.i("data pocket", "get user")
            //Тогда вынимаем его из БД или создаем
            user = mgr.user
        }
        return user
    }

    fun storeUser(mgr: DatabaseManager) {
        if (user != null) mgr.putUser(user)
    } /*public void setUser(User v){
		user = v;
	}*/
}