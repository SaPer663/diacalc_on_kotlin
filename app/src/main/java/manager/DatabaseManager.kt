package org.diacalc.android.manager

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.diacalc.android.maths.Factors
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import java.util.ArrayList

class DatabaseManager(context: android.content.Context) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $userTable")
        db.execSQL("DROP TABLE IF EXISTS $productsTable")
        db.execSQL("DROP TABLE IF EXISTS $groupTable")
        db.execSQL("DROP TABLE IF EXISTS $menuTable")
        db.execSQL("CREATE TABLE " + userTable + " (" +
                USER_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                USER_LOGIN + " TEXT, " +
                USER_PASS + " TEXT, " +
                USER_SERVER + " TEXT, " +
                USER_MMOL + " INTEGER NOT NULL, " +
                USER_PLASMA + " INTEGER NOT NULL, " +
                USER_TARGET_SUGAR + " REAL NOT NULL, " +
                USER_LOW_SUGAR + " REAL, " +
                USER_HIGH_SUGAR + " REAL, " +
                USER_ROUND + " INTEGER, " +
                USER_BASE_UNIT + " REAL NOT NULL, " +
                USER_K1 + " REAL NOT NULL, " +
                USER_K2 + " REAL NOT NULL, " +
                USER_UNIT_COST_OF_INSULIN + " REAL NOT NULL, " +
                USER_HIGH_VALUE_SUGAR + " REAL NOT NULL, " +
                USER_TARGET_VALUE_SUGAR + " REAL NOT NULL, " +
                USER_TIME_SENSE + " INTEGER NOT NULL, " +
                USER_MENU_INFO + " INTEGER);")
        //db.execSQL() Тут нужно внести нового дефолтного пользователя
        val user: org.diacalc.android.maths.User = org.diacalc.android.maths.User("noname", "", org.diacalc.android.maths.User.DEF_SERVER, Factors(),
                org.diacalc.android.maths.User.ROUND_1, org.diacalc.android.maths.User.WHOLE, org.diacalc.android.maths.User.MMOL, 5.6f, 5.6f,
                org.diacalc.android.maths.User.NO_TIMESENSE, 5.6f, 3.2f, 7.8f, 0)
        val contentValues = ContentValues(16)
        contentValues.put(USER_LOGIN, user.login)
        contentValues.put(USER_PASS, user.pass)
        contentValues.put(USER_SERVER, user.server)
        contentValues.put(USER_K1, user.factorsProperty.k1Value)
        contentValues.put(USER_K2, user.factorsProperty.getK2())
        contentValues.put(USER_UNIT_COST_OF_INSULIN, user.factorsProperty.unitCostOfInsulin)
        contentValues.put(USER_BASE_UNIT, user.factorsProperty.baseUnitValue)
        contentValues.put(USER_ROUND, user.round)
        contentValues.put(USER_PLASMA, if (user.isPlasma) 1 else 0) //Если плазма, то пишем 1, иначе нуль
        contentValues.put(USER_MMOL, if (user.isMmol) 1 else 0)
        contentValues.put(USER_HIGH_VALUE_SUGAR, user.highBloodSugar)
        contentValues.put(USER_TARGET_VALUE_SUGAR, user.bloodSugarTargets)
        contentValues.put(USER_TIME_SENSE, if (user.isTimeSense) 1 else 0)
        contentValues.put(USER_TARGET_SUGAR, user.targetSugar)
        contentValues.put(USER_LOW_SUGAR, user.lowSugar)
        contentValues.put(USER_HIGH_SUGAR, user.hiSugar)
        contentValues.put(USER_MENU_INFO, user.menuInfo)
        db.insert(userTable, null, contentValues)

        //create groupTable
        db.execSQL("CREATE TABLE " + groupTable + " (" +
                GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                GROUP_NAME + " TEXT NOT NULL, " +
                GROUP_SORT_INDEX + " INTEGER);")

        //create productsTable
        db.execSQL("CREATE TABLE " + productsTable + " (" +
                PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                PROD_NAME + " TEXT NOT NULL, " +
                PROD_PROTEINS + " REAL NOT NULL, " +
                PROD_FATS + " REAL NOT NULL, " +
                PROD_CARBS + " REAL NOT NULL, " +
                PROD_GI + " INTEGER NOT NULL, " +
                PROD_WEIGHT + " REAL NOT NULL, " +
                PROD_MOBILE + " INTEGER NOT NULL, " +
                PROD_OWNER + " INTEGER NOT NULL, " +
                PROD_USAGE + " INTEGER  NOT NULL  DEFAULT (0));")

        //create menu table
        db.execSQL("CREATE TABLE " + menuTable + " (" +
                PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +  //Индекс
                PROD_NAME + " TEXT NOT NULL, " +
                PROD_PROTEINS + " REAL NOT NULL, " +
                PROD_FATS + " REAL NOT NULL, " +
                PROD_CARBS + " REAL NOT NULL, " +
                PROD_GI + " INTEGER NOT NULL, " +
                PROD_WEIGHT + " REAL, " +
                MENU_SNACK + " INTEGER);") //перекус ли, оставляем на будущее

        //db.close();
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //do nothing yet
        db.execSQL("DROP TABLE  IF EXISTS $userTable")
        db.execSQL("DROP TABLE  IF EXISTS $groupTable")
        db.execSQL("DROP TABLE  IF EXISTS $productsTable")
        db.execSQL("DROP TABLE  IF EXISTS $menuTable")
        onCreate(db)
    }

    val menuProducts: ArrayList<ProductInMenu>
        get() {
            val db: SQLiteDatabase = readableDatabase
            val productsInMenu: ArrayList<ProductInMenu> = ArrayList()
            val cursor: android.database.Cursor = db.rawQuery("SELECT * FROM $menuTable ORDER BY $PROD_NAME", null)
            cursor.moveToFirst()
            if (cursor.count > 0) do {
                productsInMenu.add(
                        ProductInMenu(
                                cursor.getString(cursor.getColumnIndexOrThrow(PROD_NAME)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_PROTEINS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_FATS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_CARBS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_GI)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_WEIGHT)),
                                -1)
                )
            } while (cursor.moveToNext())
            cursor.close()
            db.close()
            return productsInMenu
        }

    val productsInBase: ArrayList<ProductInBase>
        get() {
            val products: ArrayList<ProductInBase> = ArrayList()
            val db: SQLiteDatabase = readableDatabase
            val cursor: android.database.Cursor = db.rawQuery("SELECT * FROM $productsTable ORDER BY $PROD_NAME", null)
            cursor.moveToFirst()
            if (cursor.count > 0) do {
                products.add(
                        ProductInBase(
                                cursor.getString(cursor.getColumnIndexOrThrow(PROD_NAME)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_PROTEINS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_FATS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_CARBS)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_GI)),
                                cursor.getFloat(cursor.getColumnIndexOrThrow(PROD_WEIGHT)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(PROD_MOBILE)) == 1,
                                cursor.getInt(cursor.getColumnIndex(PROD_OWNER)),
                                cursor.getInt(cursor.getColumnIndex(PROD_USAGE)),
                                cursor.getInt(cursor.getColumnIndex(PROD_ID)))
                )
            } while (cursor.moveToNext())
            cursor.close()
            db.close()
            return products
        }

    val productsGroups: ArrayList<ProductGroup>
        get() {
            val groups: ArrayList<ProductGroup> = ArrayList()
            val db: SQLiteDatabase = readableDatabase
            val cursor: android.database.Cursor = db.rawQuery("SELECT * FROM $groupTable ORDER BY $GROUP_SORT_INDEX", null)
            cursor.moveToFirst()
            if (cursor.count > 0) do {
                groups.add(
                        ProductGroup(
                                cursor.getString(cursor.getColumnIndexOrThrow(GROUP_NAME)),
                                cursor.getInt(cursor.getColumnIndex(GROUP_ID)))
                )
            } while (cursor.moveToNext())
            cursor.close()
            db.close()
            return groups
        }

    fun putProducts(productsGroup: ArrayList<ProductGroup>, productsInBase: ArrayList<ProductInBase>) {
        val db: SQLiteDatabase = this.writableDatabase
        //Чистим
        db.delete(groupTable, null, null)
        db.delete(productsTable, null, null)
        for (group in productsGroup.indices) {
            val contentValuesGroup = ContentValues(2)
            contentValuesGroup.put(GROUP_NAME, productsGroup[group].name)
            contentValuesGroup.put(GROUP_SORT_INDEX, group + 1)
            val groupId: Long = db.insert(groupTable, null, contentValuesGroup)
            android.util.Log.i("sqlite", "" + groupId)
            db.beginTransaction()
            try {
                for (product in productsInBase.indices) {
                    if (productsInBase[product].owner == productsGroup[group].id) {
                        val contentValueProduct = ContentValues(7)
                        contentValueProduct.put(PROD_NAME, productsInBase[product].name)
                        contentValueProduct.put(PROD_PROTEINS, productsInBase[product].proteins)
                        contentValueProduct.put(PROD_FATS, productsInBase[product].fats)
                        contentValueProduct.put(PROD_CARBS, productsInBase[product].carbohydrates)
                        contentValueProduct.put(PROD_GI, productsInBase[product].getGi())
                        contentValueProduct.put(PROD_WEIGHT, productsInBase[product].getWeight())
                        contentValueProduct.put(PROD_MOBILE, if (productsInBase[product].isMobile) 1 else 0)
                        contentValueProduct.put(PROD_OWNER, groupId)
                        contentValueProduct.put(PROD_USAGE, productsInBase[product].usage)
                        db.insert(productsTable, null, contentValueProduct)
                    }
                }
                db.setTransactionSuccessful()
            } catch (e: android.database.SQLException) {
            } finally {
                db.endTransaction()
            }
        }
        db.close()
    }

    fun putMenuProducts(productsInMenu: ArrayList<ProductInMenu>) {
        val db: SQLiteDatabase = this.writableDatabase
        //Сначала очищаем таблицу
        //db.rawQuery("DELETE FROM "+menuTable, null);
        db.delete(menuTable, null, null)
        db.beginTransaction()
        try {
            for (product in productsInMenu.indices) {
                //    db.insert(SOME_TABLE, null, SOME_VALUE);
                val contentValues = ContentValues(7)
                contentValues.put(PROD_NAME, productsInMenu[product].name)
                contentValues.put(PROD_PROTEINS, productsInMenu[product].proteins)
                contentValues.put(PROD_FATS, productsInMenu[product].fats)
                contentValues.put(PROD_CARBS, productsInMenu[product].carbohydrates)
                contentValues.put(PROD_GI, productsInMenu[product].getGi())
                contentValues.put(PROD_WEIGHT, productsInMenu[product].getWeight())
                contentValues.put(MENU_SNACK, 0)
                db.insert(menuTable, null, contentValues)
            }
            db.setTransactionSuccessful()
        } catch (e: android.database.SQLException) {
        } finally {
            db.endTransaction()
        }
        db.close()
    }

    fun deleteProduct(productInBase: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        db.delete(productsTable, "$PROD_ID=?", arrayOf("" + productInBase.id))
        db.close()
    }

    fun insertProduct(productInBase: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        val contentValues = ContentValues(9)
        contentValues.put(PROD_NAME, productInBase.name)
        contentValues.put(PROD_PROTEINS, productInBase.proteins)
        contentValues.put(PROD_FATS, productInBase.fats)
        contentValues.put(PROD_CARBS, productInBase.carbohydrates)
        contentValues.put(PROD_GI, productInBase.getGi())
        contentValues.put(PROD_WEIGHT, productInBase.getWeight())
        contentValues.put(PROD_MOBILE, 1)
        contentValues.put(PROD_OWNER, productInBase.owner)
        contentValues.put(PROD_USAGE, 0)
        productInBase.id = (db.insert(productsTable, null, contentValues).toInt())
        db.close()
    }

    fun changeProduct(productInBase: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        val contentValues = ContentValues(9)
        contentValues.put(PROD_NAME, productInBase.name)
        contentValues.put(PROD_PROTEINS, productInBase.proteins)
        contentValues.put(PROD_FATS, productInBase.fats)
        contentValues.put(PROD_CARBS, productInBase.carbohydrates)
        contentValues.put(PROD_GI, productInBase.getGi())
        contentValues.put(PROD_WEIGHT, productInBase.getWeight())
        contentValues.put(PROD_MOBILE, if (productInBase.isMobile) 1 else 0)
        contentValues.put(PROD_OWNER, productInBase.owner)
        contentValues.put(PROD_USAGE, productInBase.usage)
        db.update(productsTable, contentValues, "$PROD_ID=?", arrayOf("" + productInBase.id))
        db.close()
    }

    val user: org.diacalc.android.maths.User
        get() {
            val db: SQLiteDatabase = readableDatabase
            val cursor: android.database.Cursor = db.rawQuery("SELECT * FROM $userTable", null)
            var user: User?
            cursor.moveToFirst()
            user = if (cursor.count > 0) {
                org.diacalc.android.maths.User(
                        cursor.getString(cursor.getColumnIndex(USER_LOGIN)),
                        cursor.getString(cursor.getColumnIndex(USER_PASS)),
                        cursor.getString(cursor.getColumnIndex(USER_SERVER)),
                        Factors(
                                cursor.getFloat(cursor.getColumnIndex(USER_K1)),
                                cursor.getFloat(cursor.getColumnIndex(USER_K2)),
                                cursor.getFloat(cursor.getColumnIndex(USER_UNIT_COST_OF_INSULIN)),
                                cursor.getFloat(cursor.getColumnIndex(USER_BASE_UNIT)),
                                Factors.DIRECT
                        ),
                        cursor.getInt(cursor.getColumnIndex(USER_ROUND)),
                        cursor.getInt(cursor.getColumnIndex(USER_PLASMA)) == 1,
                        cursor.getInt(cursor.getColumnIndex(USER_MMOL)) == 1,
                        cursor.getFloat(cursor.getColumnIndex(USER_HIGH_VALUE_SUGAR)),
                        cursor.getFloat(cursor.getColumnIndex(USER_TARGET_VALUE_SUGAR)),
                        cursor.getInt(cursor.getColumnIndex(USER_TIME_SENSE)) == 1,
                        cursor.getFloat(cursor.getColumnIndex(USER_TARGET_SUGAR)),
                        cursor.getFloat(cursor.getColumnIndex(USER_LOW_SUGAR)),
                        cursor.getFloat(cursor.getColumnIndex(USER_HIGH_SUGAR)),
                        cursor.getInt(cursor.getColumnIndex(USER_MENU_INFO))
                )
            } else {
                org.diacalc.android.maths.User("noname", "", org.diacalc.android.maths.User.DEF_SERVER, Factors(),
                        org.diacalc.android.maths.User.ROUND_1, org.diacalc.android.maths.User.WHOLE, org.diacalc.android.maths.User.MMOL, 5.6f, 5.6f,
                        org.diacalc.android.maths.User.NO_TIMESENSE, 5.6f, 3.2f, 7.8f, 0)
            }
            cursor.close()
            db.close()
            return user
        }

    fun putUser(user: org.diacalc.android.maths.User) {
        val db: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues(16)
        contentValues.apply { 
            put(USER_LOGIN, user.login)
            put(USER_PASS, user.pass)
            put(USER_SERVER, user.server)
            put(USER_K1, user.factorsProperty.k1Value)
            put(USER_K2, user.factorsProperty.getK2())
            put(USER_UNIT_COST_OF_INSULIN, user.factorsProperty.unitCostOfInsulin)
            put(USER_BASE_UNIT, user.factorsProperty.baseUnitValue)
            put(USER_ROUND, user.round)
            put(USER_PLASMA, if (user.isPlasma) 1 else 0) //Если плазма, то пишем 1, иначе нуль
            put(USER_MMOL, if (user.isMmol) 1 else 0)
            put(USER_HIGH_VALUE_SUGAR, user.highBloodSugar)
            put(USER_TARGET_VALUE_SUGAR, user.bloodSugarTargets)
            put(USER_TIME_SENSE, if (user.isTimeSense) 1 else 0)
            put(USER_TARGET_SUGAR, user.targetSugar)
            put(USER_LOW_SUGAR, user.lowSugar)
            put(USER_HIGH_SUGAR, user.hiSugar)
            put(USER_MENU_INFO, user.menuInfo) }
        db.update(userTable, contentValues, null, null)
        db.close()
    }



    companion object {
        const val dbName = "dcjmobile"
        const val dbVersion = 3
        const val userTable = "user"
        const val groupTable = "prodgr"
        const val productsTable = "products"
        const val menuTable = "menu"
        private const val PROD_ID = "idProd"
        private const val PROD_NAME = "name"
        private const val PROD_PROTEINS = "prot"
        private const val PROD_FATS = "fat"
        private const val PROD_CARBS = "carb"
        private const val PROD_GI = "gi"
        private const val PROD_WEIGHT = "weight"
        private const val PROD_MOBILE = "mobile"
        private const val PROD_OWNER = "owner"
        private const val PROD_USAGE = "usage"
        private const val MENU_SNACK = "isSnack"
        private const val USER_ID = "idUser"
        private const val USER_LOGIN = "login"
        private const val USER_PASS = "pass"
        private const val USER_SERVER = "server"
        private const val USER_MMOL = "mmol"
        private const val USER_PLASMA = "plasma"
        private const val USER_TARGET_SUGAR = "targetSh"
        private const val USER_LOW_SUGAR = "lowSugar"
        private const val USER_HIGH_SUGAR = "hiSugar"
        private const val USER_ROUND = "round"
        private const val USER_BASE_UNIT = "BE"
        private const val USER_K1 = "k1"
        private const val USER_K2 = "k2"
        private const val USER_UNIT_COST_OF_INSULIN = "k3"
        private const val USER_HIGH_VALUE_SUGAR = "sh1"
        private const val USER_TARGET_VALUE_SUGAR = "sh2"
        private const val USER_TIME_SENSE = "timeSense"
        private const val USER_MENU_INFO = "menuInfo"
        private const val GROUP_ID = "idGroup"
        private const val GROUP_NAME = "name"
        private const val GROUP_SORT_INDEX = "sortIndx"
    }
}