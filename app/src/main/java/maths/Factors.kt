package org.diacalc.android.maths

class Factors {
    private var k1 = 0f
    private var k2: Float
    var k3: Float
    private var be: Float

    constructor() {
        k1 = 1f
        k2 = 0f
        k3 = 10f
        be = 10f
    }

    private fun convertK1toIndirect(newK1: Float, newXE: Float): Float {
        return if (newK1 > 0) 10 * newXE / newK1 else 0f
    }

    constructor(newk1: Float, newk2: Float, newk3: Float, newXE: Float,
                direction: Boolean) {
        if (direction) {
            k1 = if (newk1 >= 0) newk1 else 10f
            k1 = convertK1toIndirect(k1, if (newXE > 0) newXE else 1f)
        } else k1 = if (newk1 >= 0) newk1 else 1f
        k2 = if (newk2 >= 0) newk2 else 0f
        k3 = newk3
        //Тут пересчет не нужен, т.к. к1 все равно пересчитывается и приводится к
        //значению хе 10 гр.
        be = if (direction) 10f else if (newXE > 0) newXE else 10f
    }

    constructor(newk1: Float, newk2: Float, newk3: Float, newBE: Float) {
        k1 = if (newk1 >= 0) newk1 else 1f
        k2 = newk2
        k3 = newk3
        be = if (newBE > 0) newBE else 10f
    }

    constructor(newFs: Factors) {
        k1 = newFs.getK1(DIRECT)
        k2 = newFs.getK2()
        k3 = newFs.k3
        be = newFs.getBE(DIRECT)
    }

    var k1Value: Float
        get() = k1
        set(v) {
            k1 = if (v >= 0) v else 1f
        }

    var bEValue: Float
        get() = be
        set(v) {
            be = if (v > 0) v else 10f
        }

    fun getK1(direction: Boolean): Float {
        return if (direction) if (k1 > 0) be / k1 else 0f else k1
    }

    fun getK2(): Float {
        return k2
    }



    fun getBE(direction: Boolean): Float {
        return if (direction) 1f else be
    }

    fun setK1XE(newk1: Float, newXE: Float, direction: Boolean) {
        if (direction) {
            k1 = convertK1toIndirect(if (newk1 >= 0) newk1 else 10f, if (newXE > 0) newXE else 1f)
            be = 10f
        } else {
            k1 = if (newk1 >= 0) newk1 else 1f
            be = if (newXE > 0) newXE else 10f
        }
    }

    fun setK2(newk2: Float) {
        k2 = if (newk2 >= 0) newk2 else 0f
    }

    companion object {
        const val INDIRECT = true
        const val DIRECT = false
    }
}