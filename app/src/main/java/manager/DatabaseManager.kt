package org.diacalc.android.manager

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.diacalc.android.maths.Factors
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import java.util.ArrayList

class DatabaseManager(context: android.content.Context?) : SQLiteOpenHelper(context, dbName, null, dbVersion) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $userTable")
        db.execSQL("DROP TABLE IF EXISTS $prodsTable")
        db.execSQL("DROP TABLE IF EXISTS $groupTable")
        db.execSQL("DROP TABLE IF EXISTS $menuTable")
        db.execSQL("CREATE TABLE " + userTable + " (" +
                USER_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                USER_LOGIN + " TEXT, " +
                USER_PASS + " TEXT, " +
                USER_SERVER + " TEXT, " +
                USER_MMOL + " INTEGER NOT NULL, " +
                USER_PLASMA + " INTEGER NOT NULL, " +
                USER_TARGET + " REAL NOT NULL, " +
                USER_LOW + " REAL, " +
                USER_HI + " REAL, " +
                USER_ROUND + " INTEGER, " +
                USER_BE + " REAL NOT NULL, " +
                USER_K1 + " REAL NOT NULL, " +
                USER_K2 + " REAL NOT NULL, " +
                USER_K3 + " REAL NOT NULL, " +
                USER_S1 + " REAL NOT NULL, " +
                USER_S2 + " REAL NOT NULL, " +
                USER_TIME_SENSE + " INTEGER NOT NULL, " +
                USER_MENU_INFO + " INTEGER);")
        //db.execSQL() Тут нужно внести нового дефолтного пользователя
        val us: org.diacalc.android.maths.User = org.diacalc.android.maths.User("noname", "", org.diacalc.android.maths.User.DEF_SERVER, Factors(),
                org.diacalc.android.maths.User.ROUND_1, org.diacalc.android.maths.User.WHOLE, org.diacalc.android.maths.User.MMOL, 5.6f, 5.6f,
                org.diacalc.android.maths.User.NO_TIMESENSE, 5.6f, 3.2f, 7.8f, 0)
        val cv = ContentValues(16)
        cv.put(USER_LOGIN, us.login)
        cv.put(USER_PASS, us.pass)
        cv.put(USER_SERVER, us.server)
        cv.put(USER_K1, us.factors.k1Value)
        cv.put(USER_K2, us.factors.getK2())
        cv.put(USER_K3, us.factors.k3)
        cv.put(USER_BE, us.factors.bEValue)
        cv.put(USER_ROUND, us.round)
        cv.put(USER_PLASMA, if (us.isPlasma) 1 else 0) //Если плазма, то пишем 1, иначе нуль
        cv.put(USER_MMOL, if (us.isMmol) 1 else 0)
        cv.put(USER_S1, us.s1)
        cv.put(USER_S2, us.s2)
        cv.put(USER_TIME_SENSE, if (us.isTimeSense) 1 else 0)
        cv.put(USER_TARGET, us.targetSugar)
        cv.put(USER_LOW, us.lowSugar)
        cv.put(USER_HI, us.hiSugar)
        cv.put(USER_MENU_INFO, us.menuInfo)
        db.insert(userTable, null, cv)

        //create groupTable
        db.execSQL("CREATE TABLE " + groupTable + " (" +
                GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                GROUP_NAME + " TEXT NOT NULL, " +
                GROUP_SORT_INDX + " INTEGER);")

        //create productsTable
        db.execSQL("CREATE TABLE " + prodsTable + " (" +
                PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                PROD_NAME + " TEXT NOT NULL, " +
                PROD_PROT + " REAL NOT NULL, " +
                PROD_FAT + " REAL NOT NULL, " +
                PROD_CARB + " REAL NOT NULL, " +
                PROD_GI + " INTEGER NOT NULL, " +
                PROD_WEIGHT + " REAL NOT NULL, " +
                PROD_MOBILE + " INTEGER NOT NULL, " +
                PROD_OWNER + " INTEGER NOT NULL, " +
                PROD_USAGE + " INTEGER  NOT NULL  DEFAULT (0));")

        //create menu table
        db.execSQL("CREATE TABLE " + menuTable + " (" +
                PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +  //Индекс
                PROD_NAME + " TEXT NOT NULL, " +
                PROD_PROT + " REAL NOT NULL, " +
                PROD_FAT + " REAL NOT NULL, " +
                PROD_CARB + " REAL NOT NULL, " +
                PROD_GI + " INTEGER NOT NULL, " +
                PROD_WEIGHT + " REAL, " +
                MENU_SNACK + " INTEGER);") //перекус ли, оставляем на будущее

        //db.close();
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //do nothing yet
        db.execSQL("DROP TABLE  IF EXISTS $userTable")
        db.execSQL("DROP TABLE  IF EXISTS $groupTable")
        db.execSQL("DROP TABLE  IF EXISTS $prodsTable")
        db.execSQL("DROP TABLE  IF EXISTS $menuTable")
        onCreate(db)
    }

    val menuProducts: java.util.ArrayList<ProductInMenu>
        get() {
            val db: SQLiteDatabase = readableDatabase
            val prods: java.util.ArrayList<ProductInMenu> = ArrayList()
            val c: android.database.Cursor = db.rawQuery("SELECT * FROM $menuTable ORDER BY $PROD_NAME", null)
            c.moveToFirst()
            if (c.count > 0) do {
                prods.add(
                        ProductInMenu(
                                c.getString(c.getColumnIndexOrThrow(PROD_NAME)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_PROT)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_FAT)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_CARB)),
                                c.getInt(c.getColumnIndexOrThrow(PROD_GI)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_WEIGHT)),
                                -1)
                )
            } while (c.moveToNext())
            c.close()
            db.close()
            return prods
        }

    val products: java.util.ArrayList<ProductInBase>
        get() {
            val prods: java.util.ArrayList<ProductInBase> = ArrayList()
            val db: SQLiteDatabase = readableDatabase
            val c: android.database.Cursor = db.rawQuery("SELECT * FROM $prodsTable ORDER BY $PROD_NAME", null)
            c.moveToFirst()
            if (c.count > 0) do {
                prods.add(
                        ProductInBase(
                                c.getString(c.getColumnIndexOrThrow(PROD_NAME)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_PROT)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_FAT)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_CARB)),
                                c.getInt(c.getColumnIndexOrThrow(PROD_GI)),
                                c.getFloat(c.getColumnIndexOrThrow(PROD_WEIGHT)),
                                c.getInt(c.getColumnIndexOrThrow(PROD_MOBILE)) == 1,
                                c.getInt(c.getColumnIndex(PROD_OWNER)),
                                c.getInt(c.getColumnIndex(PROD_USAGE)),
                                c.getInt(c.getColumnIndex(PROD_ID)))
                )
            } while (c.moveToNext())
            c.close()
            db.close()
            return prods
        }

    val groups: ArrayList<ProductGroup>
        get() {
            val groups: ArrayList<ProductGroup> = ArrayList()
            val db: SQLiteDatabase = readableDatabase
            val c: android.database.Cursor = db.rawQuery("SELECT * FROM $groupTable ORDER BY $GROUP_SORT_INDX", null)
            c.moveToFirst()
            if (c.count > 0) do {
                groups.add(
                        ProductGroup(
                                c.getString(c.getColumnIndexOrThrow(GROUP_NAME)),
                                c.getInt(c.getColumnIndex(GROUP_ID)))
                )
            } while (c.moveToNext())
            c.close()
            db.close()
            return groups
        }

    fun putProducts(gr: ArrayList<ProductGroup>?, pr: ArrayList<ProductInBase>?) {
        val db: SQLiteDatabase = this.writableDatabase
        //Чистим
        db.delete(groupTable, null, null)
        db.delete(prodsTable, null, null)
        if (gr != null) {
            for (g in gr.indices) {
                val cvG = ContentValues(2)
                cvG.put(GROUP_NAME, gr[g]?.name)
                cvG.put(GROUP_SORT_INDX, g + 1)
                val gId: Long = db.insert(groupTable, null, cvG)
                android.util.Log.i("sqlite", "" + gId)
                db.beginTransaction()
                try {
                    if (pr != null) {
                        for (p in pr.indices) {
                            if (pr[p]?.owner == gr[g]?.id) {
                                val cv_p = ContentValues(7)
                                cv_p.put(PROD_NAME, pr[p]?.name)
                                cv_p.put(PROD_PROT, pr[p]?.prot)
                                cv_p.put(PROD_FAT, pr[p]?.fat)
                                cv_p.put(PROD_CARB, pr[p]?.carb)
                                cv_p.put(PROD_GI, pr[p]?.getGi())
                                cv_p.put(PROD_WEIGHT, pr[p]?.getWeight())
                                cv_p.put(PROD_MOBILE, if (pr[p]?.isMobile!!) 1 else 0)
                                cv_p.put(PROD_OWNER, gId)
                                cv_p.put(PROD_USAGE, pr[p]?.usage)
                                db.insert(prodsTable, null, cv_p)
                            }
                        }
                    }
                    db.setTransactionSuccessful()
                } catch (e: android.database.SQLException) {
                } finally {
                    db.endTransaction()
                }
            }
        }
        db.close()
    }

    fun putMenuProds(prods: ArrayList<ProductInMenu>?) {
        val db: SQLiteDatabase = this.writableDatabase
        //Сначала очищаем таблицу
        //db.rawQuery("DELETE FROM "+menuTable, null);
        db.delete(menuTable, null, null)
        db.beginTransaction()
        try {
            if (prods != null) {
                for (i in prods.indices) {
                    //    db.insert(SOME_TABLE, null, SOME_VALUE);
                    val cv = ContentValues(7)
                    cv.put(PROD_NAME, prods[i].name)
                    cv.put(PROD_PROT, prods[i].prot)
                    cv.put(PROD_FAT, prods[i].fat)
                    cv.put(PROD_CARB, prods[i].carb)
                    cv.put(PROD_GI, prods[i].getGi())
                    cv.put(PROD_WEIGHT, prods[i].getWeight())
                    cv.put(MENU_SNACK, 0)
                    db.insert(menuTable, null, cv)
                }
            }
            db.setTransactionSuccessful()
        } catch (e: android.database.SQLException) {
        } finally {
            db.endTransaction()
        }
        db.close()
    }

    fun deleteProduct(p: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        db.delete(prodsTable, "$PROD_ID=?", arrayOf("" + p.id))
        db.close()
    }

    fun insertProduct(prod: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        val cv = ContentValues(9)
        cv.put(PROD_NAME, prod.name)
        cv.put(PROD_PROT, prod.prot)
        cv.put(PROD_FAT, prod.fat)
        cv.put(PROD_CARB, prod.carb)
        cv.put(PROD_GI, prod.getGi())
        cv.put(PROD_WEIGHT, prod.getWeight())
        cv.put(PROD_MOBILE, 1)
        cv.put(PROD_OWNER, prod.owner)
        cv.put(PROD_USAGE, 0)
        prod.id = (db.insert(prodsTable, null, cv) as Int)
        db.close()
    }

    fun changeProduct(prod: ProductInBase) {
        val db: SQLiteDatabase = writableDatabase
        val cv = ContentValues(9)
        cv.put(PROD_NAME, prod.name)
        cv.put(PROD_PROT, prod.prot)
        cv.put(PROD_FAT, prod.fat)
        cv.put(PROD_CARB, prod.carb)
        cv.put(PROD_GI, prod.getGi())
        cv.put(PROD_WEIGHT, prod.getWeight())
        cv.put(PROD_MOBILE, if (prod.isMobile) 1 else 0)
        cv.put(PROD_OWNER, prod.owner)
        cv.put(PROD_USAGE, prod.usage)
        db.update(prodsTable, cv, "$PROD_ID=?", arrayOf("" + prod.id))
        db.close()
    }

    val user: org.diacalc.android.maths.User?
        get() {
            val db: SQLiteDatabase = readableDatabase
            val c: android.database.Cursor = db.rawQuery("SELECT * FROM $userTable", null)
            var user: org.diacalc.android.maths.User? = null
            c.moveToFirst()
            user = if (c.count > 0) {
                org.diacalc.android.maths.User(
                        c.getString(c.getColumnIndex(USER_LOGIN)),
                        c.getString(c.getColumnIndex(USER_PASS)),
                        c.getString(c.getColumnIndex(USER_SERVER)),
                        Factors(
                                c.getFloat(c.getColumnIndex(USER_K1)),
                                c.getFloat(c.getColumnIndex(USER_K2)),
                                c.getFloat(c.getColumnIndex(USER_K3)),
                                c.getFloat(c.getColumnIndex(USER_BE)),
                                Factors.DIRECT
                        ),
                        c.getInt(c.getColumnIndex(USER_ROUND)),
                        c.getInt(c.getColumnIndex(USER_PLASMA)) == 1,
                        c.getInt(c.getColumnIndex(USER_MMOL)) == 1,
                        c.getFloat(c.getColumnIndex(USER_S1)),
                        c.getFloat(c.getColumnIndex(USER_S2)),
                        c.getInt(c.getColumnIndex(USER_TIME_SENSE)) == 1,
                        c.getFloat(c.getColumnIndex(USER_TARGET)),
                        c.getFloat(c.getColumnIndex(USER_LOW)),
                        c.getFloat(c.getColumnIndex(USER_HI)),
                        c.getInt(c.getColumnIndex(USER_MENU_INFO))
                )
            } else {
                org.diacalc.android.maths.User("noname", "", org.diacalc.android.maths.User.DEF_SERVER, Factors(),
                        org.diacalc.android.maths.User.ROUND_1, org.diacalc.android.maths.User.WHOLE, org.diacalc.android.maths.User.MMOL, 5.6f, 5.6f,
                        org.diacalc.android.maths.User.NO_TIMESENSE, 5.6f, 3.2f, 7.8f, 0)
            }
            c.close()
            db.close()
            return user
        }

    fun putUser(us: org.diacalc.android.maths.User) {
        val db: SQLiteDatabase = this.writableDatabase
        val cv = ContentValues(16)
        cv.put(USER_LOGIN, us.login)
        cv.put(USER_PASS, us.pass)
        cv.put(USER_SERVER, us.server)
        cv.put(USER_K1, us.factors.k1Value)
        cv.put(USER_K2, us.factors.getK2())
        cv.put(USER_K3, us.factors.k3)
        cv.put(USER_BE, us.factors.bEValue)
        cv.put(USER_ROUND, us.round)
        cv.put(USER_PLASMA, if (us.isPlasma) 1 else 0) //Если плазма, то пишем 1, иначе нуль
        cv.put(USER_MMOL, if (us.isMmol) 1 else 0)
        cv.put(USER_S1, us.s1)
        cv.put(USER_S2, us.s2)
        cv.put(USER_TIME_SENSE, if (us.isTimeSense) 1 else 0)
        cv.put(USER_TARGET, us.targetSugar)
        cv.put(USER_LOW, us.lowSugar)
        cv.put(USER_HI, us.hiSugar)
        cv.put(USER_MENU_INFO, us.menuInfo)
        db.update(userTable, cv, null, null)
        db.close()
    }



    companion object {
        const val dbName = "dcjmobile"
        const val dbVersion = 3
        const val userTable = "user"
        const val groupTable = "prodgr"
        const val prodsTable = "products"
        const val menuTable = "menu"
        private const val PROD_ID = "idProd"
        private const val PROD_NAME = "name"
        private const val PROD_PROT = "prot"
        private const val PROD_FAT = "fat"
        private const val PROD_CARB = "carb"
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
        private const val USER_TARGET = "targetSh"
        private const val USER_LOW = "lowSugar"
        private const val USER_HI = "hiSugar"
        private const val USER_ROUND = "round"
        private const val USER_BE = "BE"
        private const val USER_K1 = "k1"
        private const val USER_K2 = "k2"
        private const val USER_K3 = "k3"
        private const val USER_S1 = "sh1"
        private const val USER_S2 = "sh2"
        private const val USER_TIME_SENSE = "timeSense"
        private const val USER_MENU_INFO = "menuInfo"
        private const val GROUP_ID = "idGroup"
        private const val GROUP_NAME = "name"
        private const val GROUP_SORT_INDX = "sortIndx"
    }
}