package org.diacalc.android

import org.diacalc.android.manager.DatabaseManager
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu

class DataPocket : android.app.Application() {
    private var user: User? = null
    private var menuProducts: java.util.ArrayList<ProductInMenu>? = null
    private var groups: java.util.ArrayList<ProductGroup>? = null
    private var products: java.util.ArrayList<ProductInBase>? = null
    private var need2saveProds = false
    val isProductsNull: Boolean
        get() = products == null
    var isNeedToSaveMenu = false
        private set


    fun setProdsNeed2Save() {
        need2saveProds = true
    }

    fun setMenuNeedToSave() {
        isNeedToSaveMenu = true
    }

    fun getProductsFromBD(mgr: DatabaseManager): java.util.ArrayList<ProductInBase>? {
        if (products == null) { //Запросили продукты первый раз, берем из БД
            products = mgr.productsInBase
        }
        return products
    }

    fun getGroupsFromBD(mgr: DatabaseManager): java.util.ArrayList<ProductGroup>? {
        if (groups == null) { //Запросили продукты первый раз, берем из БД
            groups = mgr.productsGroups
        }
        return groups
    }

    fun getProductMenuFromBD(mgr: DatabaseManager): java.util.ArrayList<ProductInMenu>? {
        if (menuProducts == null) { //Запросили продукты первый раз, берем из БД
            menuProducts = mgr.menuProducts
        }
        return menuProducts
    }

    fun resettingToZeroPointers() {
        menuProducts = null
        user = null
    }

    fun setGroupProds2Null() {
        products = null
        groups = null
    }


    fun saveProductMenuToBD(mgr: DatabaseManager) {
        android.util.Log.i("DataPocket", "storing menu1 $isNeedToSaveMenu")
        if (isNeedToSaveMenu) {
            android.util.Log.i("DataPocket", "storing menu")
            menuProducts?.let { mgr.putMenuProducts(it) }
        }
    }

    fun getUserFromBD(mgr: DatabaseManager): User {
        if (user == null) {
            android.util.Log.i("data pocket", "get user")
            //Тогда вынимаем его из БД или создаем
            user = mgr.user
        }
        return user as User
    }

    fun saveUserToBD(mgr: DatabaseManager) {
        user?.let { mgr.putUser(it) }
    }
}