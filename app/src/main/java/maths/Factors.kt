package org.diacalc.android.maths

class Factors {
    private var k1 = 0f
    private var k2: Float
    var unitCostOfInsulin: Float
    private var baseUnit: Float

    constructor() {
        k1 = 1f
        k2 = 0f
        unitCostOfInsulin = 10f
        baseUnit = 10f
    }

    private fun convertK1toIndirect(newK1: Float, newXE: Float): Float {
        return  10 * newXE / newK1
    }

    constructor(newk1: Float, newk2: Float, newUnitCostOfInsulin: Float, newXE: Float,
                direction: Boolean) {
        k1 = if (direction) {
            convertK1toIndirect(if (newk1 >= 0) newk1 else 10f, if (newXE > 0) newXE else 1f)
        }
        else if (newk1 >= 0) newk1 else 1f
        k2 = if (newk2 >= 0) newk2 else 0f
        unitCostOfInsulin = newUnitCostOfInsulin
        //Тут пересчет не нужен, т.к. к1 все равно пересчитывается и приводится к
        //значению хе 10 гр.
        baseUnit = if (direction) 10f else if (newXE > 0) newXE else 10f
    }
/*
    constructor(newk1: Float, newk2: Float, newUnitCostOfInsulin: Float, newBaseUnit: Float) {
        k1 = if (newk1 >= 0) newk1 else 1f
        k2 = newk2
        unitCostOfInsulin = newUnitCostOfInsulin
        baseUnit = if (newBaseUnit > 0) newBaseUnit else 10f
    }
*/
    constructor(newFactors: Factors) {
        k1 = newFactors.getK1(DIRECT)
        k2 = newFactors.getK2()
        unitCostOfInsulin = newFactors.unitCostOfInsulin
        baseUnit = newFactors.getBaseUnit(DIRECT)
    }

    var k1Value: Float
        get() = k1
        set(value) {
            k1 = if (value >= 0) value else 1f
        }

    var baseUnitValue: Float
        get() = baseUnit
        set(value) {
            baseUnit = if (value > 0) value else 10f
        }

    fun getK1(direction: Boolean): Float {
        return if (direction) {
            if (k1 > 0) baseUnit / k1 else 0f
        } else k1
    }

    fun getK2(): Float {
        return k2
    }



    fun getBaseUnit(direction: Boolean): Float {
        return if (direction) 1f else baseUnit
    }

    fun setK1BaseUnit(newk1: Float, newBaseUnit: Float, direction: Boolean) {
        if (direction) {
            k1 = convertK1toIndirect(if (newk1 >= 0) newk1 else 10f, if (newBaseUnit > 0) newBaseUnit else 1f)
            baseUnit = 10f
        } else {
            k1 = if (newk1 >= 0) newk1 else 1f
            baseUnit = if (newBaseUnit > 0) newBaseUnit else 10f
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