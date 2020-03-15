package org.diacalc.android.internet

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.diacalc.android.maths.User
import org.diacalc.android.products.ProductGroup
import org.diacalc.android.products.ProductInBase
import org.diacalc.android.products.ProductInMenu
import java.util.ArrayList

//А с помощью этого класса будем делать запросы и получать ответы
class DoingPost(u: User?) {
    private val user: User? = u
    private var response: String? = null
    private var error = false
    private var errMsg: String? = ""
    private fun doPost(nameValuePairs: MutableList<NameValuePair?>?) {
        android.util.Log.i("RESPONSE", "start")
        // Create a new HttpClient and Post Header
        val httpclient: HttpClient = DefaultHttpClient()
        val httppost = HttpPost(user!!.server + "server.php")
        try {
            // Add your data
            httppost.entity = UrlEncodedFormEntity(nameValuePairs, "utf-8")

            // Execute HTTP Post Request
            android.util.Log.i("RESPONSE", "prepare")
            val responsePOST: HttpResponse = httpclient.execute(httppost)
            android.util.Log.i("RESPONSE", "done")
            val resEntity: HttpEntity = responsePOST.entity
            if (responsePOST.statusLine.statusCode === 200) {
                if (resEntity != null) {
                    response = EntityUtils.toString(resEntity)
                    if (!response?.endsWith("<ok>")!!) {
                        error = true
                        errMsg = "error's happend\n$response"
                    }
                } else {
                    error = true
                    errMsg = "no response"
                }
            } else {
                errMsg += "Wrong answer " + responsePOST.statusLine.statusCode
                error = true
            }
        } catch (e: ClientProtocolException) {
            error = true
            errMsg += e.message
        } catch (e: java.io.IOException) {
            error = true
            errMsg += e.message
        } catch (e: java.lang.Exception) {
            error = true
            errMsg += e.message
        }
    }

    fun sendMenu(prods: ArrayList<ProductInMenu>?): java.util.HashMap<String?, Any?>? {
        val nameValuePairs: MutableList<NameValuePair?> = java.util.ArrayList()
        nameValuePairs.add(BasicNameValuePair("action", ACT_SEND_MENU))
        if (user != null) {
            nameValuePairs.add(BasicNameValuePair("login", user.login))
            nameValuePairs.add(BasicNameValuePair("pass", user.pass))
            nameValuePairs.add(BasicNameValuePair("k1", "" +
                    user.factors.k1Value * 10 / user.factors.bEValue))
            nameValuePairs.add(BasicNameValuePair("k2", "" + user.factors.getK2()))
            nameValuePairs.add(BasicNameValuePair("k3", "" + user.factors.k3))
            nameValuePairs.add(BasicNameValuePair("sh1", "" + user.s1))
            nameValuePairs.add(BasicNameValuePair("sh2", "" + user.s2))
        }
        if (prods != null) {
            for (i in prods.indices) {
                nameValuePairs.add(BasicNameValuePair("names[]", prods[i]?.name))
                nameValuePairs.add(BasicNameValuePair("prots[]", "" + prods[i]!!.prot))
                nameValuePairs.add(BasicNameValuePair("fats[]", "" + prods[i]!!.fat))
                nameValuePairs.add(BasicNameValuePair("carbs[]", "" + prods[i]!!.carb))
                nameValuePairs.add(BasicNameValuePair("gis[]", "" + prods[i]!!.gi))
                nameValuePairs.add(BasicNameValuePair("weights[]", "" + prods[i]!!.getWeight()))
                nameValuePairs.add(BasicNameValuePair("issnacks[]", "0"))
            }
        }
        nameValuePairs.add(BasicNameValuePair("ok", "ok"))
        doPost(nameValuePairs)
        val hm: java.util.HashMap<String?, Any?> = java.util.HashMap()
        if (error) {
            hm.put(ERROR, errMsg)
            return hm
        }
        hm.put(ERROR, null)
        return hm
    }

    fun requestProducts(): java.util.HashMap<String?, Any?>? {
        val nameValuePairs: MutableList<NameValuePair?> = java.util.ArrayList(4)
        if (user != null) {
            nameValuePairs.add(BasicNameValuePair("login", user.login))
            nameValuePairs.add(BasicNameValuePair("pass", user.pass))
        }
        nameValuePairs.add(BasicNameValuePair("action", ACT_GET_PRODS))
        nameValuePairs.add(BasicNameValuePair("ok", "ok"))
        doPost(nameValuePairs)
        val hm: java.util.HashMap<String?, Any?> = java.util.HashMap()
        if (error) {
            hm.put(ERROR, errMsg)
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, response)
        //Теперь парсим вывод сервера
        var groupsS: String? = null
        groupsS = try {
            getFirstTagged(response, "groups")
        } catch (e: java.lang.Exception) {
            hm.put(ERROR, e.message)
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, "groups=$groupsS")
        var prodsS: String? = null
        prodsS = try {
            getFirstTagged(response, "prods")
        } catch (e: java.lang.Exception) {
            hm[ERROR] = e.message
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, "prods=$prodsS")
        val groups: java.util.ArrayList<ProductGroup?>? = parseGroups(groupsS)
        val prods: java.util.ArrayList<ProductInBase?>? = parseProds(prodsS)
        hm[PRODS_GROUP] = groups
        hm[PRODS_PRODS] = prods
        hm[ERROR] = null
        return hm
    }

    private fun parseGroups(st: String?): java.util.ArrayList<ProductGroup?>? {
        val arr: java.util.ArrayList<ProductGroup?> = java.util.ArrayList()
        val lines: Array<String?> = st?.trim { it <= ' ' }?.split("<br>")!!.toTypedArray()
        for (i in lines.indices) {
            if (lines[i]?.trim { it <= ' ' }?.isNotEmpty()!!) {
                val param: Array<String?> = lines[i]?.trim { it <= ' ' }?.split(" ")!!.toTypedArray()
                var id: Int
                if (param.size > 1) {
                    id = try {
                        param[param.size - 1]?.toInt()
                    } catch (ex: java.lang.Exception) {
                        0
                    }!!
                    var name: String? = ""
                    for (j in 0 until param.size - 1) {
                        name += param[j].toString() + " "
                    }
                    if (name != null) {
                        name = name.trim { it <= ' ' }
                    }
                    arr.add(name?.let { ProductGroup(it, id) })
                }
            }
        }
        return arr
    }

    private fun parseProds(st: String?): java.util.ArrayList<ProductInBase?>? {
        val arr: java.util.ArrayList<ProductInBase?> = java.util.ArrayList()
        val lines: Array<String?> = st?.trim { it <= ' ' }?.split("<br>")!!.toTypedArray()
        for (i in lines.indices) {
            if (lines[i]?.trim { it <= ' ' }?.isNotEmpty()!!) {
                val param: Array<String?> = lines[i]?.trim { it <= ' ' }?.split(" ")!!.toTypedArray()
                var gi: Int
                var owner: Int
                var usage: Int
                var c: Float
                var f: Float
                var p: Float
                if (param.size > 6) {
                    try {
                        owner = param[param.size - 1]!!.toInt()
                        usage = param[param.size - 2]!!.toInt()
                        gi = param[param.size - 3]!!.toInt()
                        c = param[param.size - 4]!!.toFloat()
                        f = param[param.size - 5]!!.toFloat()
                        p = param[param.size - 6]!!.toFloat()
                    } catch (ex: java.lang.Exception) {
                        //ex.printStackTrace();
                        gi = 0
                        usage = gi
                        owner = usage
                        p = 0f
                        f = p
                        c = f
                    }
                    var name: String? = ""
                    for (j in 0 until param.size - 6) {
                        name += param[j].toString() + " "
                    }
                    if (name != null) {
                        name = name.trim { it <= ' ' }
                    }
                    arr.add(ProductInBase(name, p, f, c, gi, 100f, false, owner, usage, -1))
                }
            }
        }
        return arr
    }

    fun requestMenu(): java.util.HashMap<String?, Any?>? {
        val nameValuePairs: MutableList<NameValuePair?> = java.util.ArrayList(4)
        if (user != null) {
            nameValuePairs.add(BasicNameValuePair("login", user.login))
            nameValuePairs.add(BasicNameValuePair("pass", user.pass))
        }
        nameValuePairs.add(BasicNameValuePair("action", ACT_GET_MENU))
        nameValuePairs.add(BasicNameValuePair("ok", "ok"))
        doPost(nameValuePairs)
        val hm: java.util.HashMap<String?, Any?> = java.util.HashMap()
        if (error) {
            hm.put(ERROR, errMsg)
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, response)
        //Теперь парсим вывод сервера
        var menu: String? = null
        menu = try {
            getFirstTagged(response, "menu")
        } catch (e: java.lang.Exception) {
            hm[ERROR] = e.message
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, "menu=$menu")
        var coefs: String? = null
        coefs = try {
            getFirstTagged(response, "coefs")
        } catch (e: java.lang.Exception) {
            hm[ERROR] = e.message
            return hm
        }
        android.util.Log.i(DoingPost::class.java.name, "coefs=$coefs")
        val prods: java.util.ArrayList<ProductInMenu?>? = parseMenu(menu, 0)
        val prodsSnack: java.util.ArrayList<ProductInMenu?>? = parseMenu(menu, 1)
        val us: User? = extractCoefs(coefs)
        hm[MENU_MENU] = prods
        hm[MENU_SNACK] = prodsSnack
        hm[MENU_USER] = us
        hm[ERROR] = null
        return hm
    }

    private fun extractCoefs(rowContaningCoefs: String?): User? {
        //ДБ 5 параметров
        val par: Array<String?> = rowContaningCoefs?.replace("<br>", "")?.split(" ")!!.toTypedArray()
        if (par.size != 5) {
            return null
        }
        val us: User? = user?.let { User(it) }
        if (us != null) {
            us.factors.k1Value = (par[0]!!.toFloat() *
                    us.factors.bEValue / 10f)
            par[1]?.toFloat()?.let { us.factors.setK2(it) }
            us.factors.k3 = (par[2]!!.toFloat())
            us.s1 = par[3]!!.toFloat()
            us.s2 = par[4]!!.toFloat()
        }
        return us
    }

    @Throws(java.lang.Exception::class)
    private fun getFirstTagged(output: String?, tag: String?): String? {
        var output = output
        var strs: String? = ""
        val tagfullst = "<$tag>"
        val tagfullend = "</$tag>"
        var st = 0
        var end = 0
        if (output!!.isNotEmpty()) { // && st>=0 && end>=0){
            st = output.indexOf(tagfullst)
            end = output.indexOf(tagfullend, st + tagfullst.length)
            if (st >= 0 && end >= 0) {
                strs = output.substring(st + tagfullst.length, end)
                output = output.substring(end + tagfullend.length)
            } else throw java.lang.Exception("no needed tag $tag found")
        } else throw java.lang.Exception("empty string")
        return strs
    }

    private fun parseMenu(st: String?, type: Int): java.util.ArrayList<ProductInMenu?>? {
        val arr: java.util.ArrayList<ProductInMenu?> = java.util.ArrayList()
        val lines: Array<String?> = st?.trim { it <= ' ' }?.split("<br>")!!.toTypedArray()
        for (i in lines.indices) {
            val param: Array<String?> = lines[i]?.split(" ")!!.toTypedArray()
            var gi: Int
            var w: Float
            var p: Float
            var c: Float
            var f: Float
            var name: String? = ""
            if (param.size > 6) { //0-меню 1-перекус
                try {
                    val typeP: Int = param[param.size - 1]!!.toInt()
                    if (type == typeP) {
                        w = param[param.size - 2]!!.toFloat()
                        gi = param[param.size - 3]!!.toInt()
                        c = param[param.size - 4]!!.toFloat()
                        f = param[param.size - 5]!!.toFloat()
                        p = param[param.size - 6]!!.toFloat()
                        for (j in 0 until param.size - 6) {
                            name += param[j].toString() + " "
                        }
                        if (name != null) {
                            name = name.trim { it <= ' ' }
                        }
                    } else continue  //Если не тот тип, то идем к следующему
                } catch (ex: java.lang.Exception) {
                    gi = 0
                    c = 0f
                    f = c
                    p = f
                    w = p
                }
                arr.add(ProductInMenu(name, p, f, c, gi, w, -1))
            }
        }
        return arr
    }

    companion object {
        val ERROR: String? = "error"
        val MENU_MENU: String? = "menu content"
        val MENU_SNACK: String? = "snack content"
        val MENU_USER: String? = "menu user"
        val PRODS_GROUP: String? = "group content"
        val PRODS_PRODS: String? = "products content"
        private val ACT_GET_MENU: String? = "get menu"
        private val ACT_SEND_MENU: String? = "send menu"
        private val ACT_GET_PRODS: String? = "get mobile base"
    }

}