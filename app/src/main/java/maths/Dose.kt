/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Toporov Konstantin. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL")  (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/copyleft/gpl.html  See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * You should include file containing license in each project.
 * If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by
 * the GPL Version 3, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [GPL Version 3] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under the GPL Version 3 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 3 code and therefore, elected the GPL
 * Version 3 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Toporov Konstantin.
 */
package org.diacalc.android.maths

import org.diacalc.android.products.ProductW

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
</www.diacalc.org> */
class Dose {
    private lateinit var prod: ProductW
    private lateinit var fact: Factors
    private lateinit var dps: DPS
    private var needReCalc: Boolean = false
    private var calories = 0f
    private var slowDose = 0f //медленная доза
    private var carbSlowDose = 0f
    private var carbFastDose = 0f


    constructor(newProd: ProductW?, newFs: Factors?, newDps: DPS) {
        prod = ProductW(newProd)
        fact = newFs?.let { Factors(it) }!!
        dps = newDps?.let { DPS(it) }!!
        needReCalc = true
    }

    constructor(newDose: Dose) {
        prod = ProductW(newDose.prod)
        fact = Factors(newDose.fact)
        dps = DPS(newDose.dps)
        needReCalc = true
    }

    constructor() {
        prod = ProductW()
        fact = Factors()
        dps = DPS()
        needReCalc = true
    }

    constructor(newProd: ProductW, newFs: Factors?, newDps: Unit?) {
        prod = ProductW(newProd)
        fact = newFs?.let { Factors(it) }!!
        dps = newDps?.let { DPS(it) }!!
        needReCalc = true
    }

    private fun calcDoses() {
        val kWH: Float = PROT * prod.allProt
        val kFAT: Float = FAT * prod.allFat
        val kUG: Float = CARB * prod.allCarb
        calories = kWH + kFAT + kUG
        slowDose = fact.getK2() * kWH / 100f + fact.getK2() * kFAT / 100f
        //    (    WH Doze   )   (   Fat Doze     )

        //здесь 1 надо заменить на коэффициент позволяющий учитывать углеводы
        //по количеству
        carbSlowDose = fact.getK1(Factors.DIRECT) / fact.getBE(Factors.DIRECT) *
                (prod.allCarb * (100f - prod.getGi() as Float) / 100f)
        carbFastDose = fact.getK1(Factors.DIRECT) / fact.getBE(Factors.DIRECT) *
                (prod.allCarb * prod.getGi() as Float / 100f)
        needReCalc = false
    }

    fun setProduct(newProduct: ProductW?) {
        prod = ProductW(newProduct)
        needReCalc = true
    }

    fun setFactors(newFs: Factors?) {
        fact = newFs?.let { Factors(it) }!!
        needReCalc = true
    }

    fun setDPS(newDps: DPS?) {
        dps = newDps?.let { DPS(it) }!!
        needReCalc = true
    }

    fun getSlowDose(): Float {
        if (needReCalc) calcDoses()
        return slowDose
    }

    fun getCarbSlowDose(): Float {
        if (needReCalc) calcDoses()
        return carbSlowDose
    }

    fun getCarbFastDose(): Float {
        if (needReCalc) calcDoses()
        return carbFastDose
    }

    fun getCalories(): Float {
        if (needReCalc) calcDoses()
        return calories
    }

    val wholeDose: Float
        get() {
            if (needReCalc) calcDoses()
            return slowDose + carbSlowDose + carbFastDose + dps.dPSDose
        }

    val dPSDose: Float
        get() = dps.dPSDose

    companion object {
        const val PROT = 4.0f
        const val FAT = 9.0f
        const val CARB = 4.0f
    }
}