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

import org.diacalc.android.products.ProductFeatures

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
</www.diacalc.org> */
class Dose {
    private var productFeatures: ProductFeatures
    private var factors: Factors
    private var dps: DPS
    private var needRecalculation: Boolean = false
    private var calories = 0f
    private var slowDose = 0f //медленная доза
    private var carbohydratesSlowDose = 0f
    private var carbohydratesFastDose = 0f


    constructor(newProductFeatures: ProductFeatures, newFactors: Factors, newDps: DPS) {
        productFeatures = ProductFeatures(newProductFeatures)
        factors = Factors(newFactors)
        dps = DPS(newDps)
        needRecalculation = true
    }

    constructor(newDose: Dose) {
        productFeatures = ProductFeatures(newDose.productFeatures)
        factors = Factors(newDose.factors)
        dps = DPS(newDose.dps)
        needRecalculation = true
    }

    constructor() {
        productFeatures = ProductFeatures()
        factors = Factors()
        dps = DPS()
        needRecalculation = true
    }

/*
    constructor(newMassOfProducts: ProductW, newFactors: Factors, newDps: DPS) {
        massOfProducts = ProductW(newMassOfProducts)
        factors = newFactors?.let { Factors(it) }!!
        dps = newDps?.let { DPS(it) }!!
        needRecalculation = true
    }
*/

    private fun calculateDoses() {
        val proteinsCount: Float = PROTEINS * productFeatures.allProteins
        val fatsCount: Float = FATS * productFeatures.allFats
        val carbohydratesCount: Float = CARBOHYDRATES * productFeatures.allCarbohydrates
        val productGi = productFeatures.getGi()
        calories = proteinsCount + fatsCount + carbohydratesCount
        slowDose = factors.getK2() * proteinsCount / 100f + factors.getK2() * fatsCount / 100f
        //    (    WH Doze   )   (   Fat Doze     )

        //здесь 1 надо заменить на коэффициент позволяющий учитывать углеводы
        //по количеству
        carbohydratesSlowDose = factors.getK1(Factors.DIRECT) / factors.getBaseUnit(Factors.DIRECT) *
                (productFeatures.allCarbohydrates * (100f - productGi) / 100f)
        carbohydratesFastDose = factors.getK1(Factors.DIRECT) / factors.getBaseUnit(Factors.DIRECT) *
                (productFeatures.allCarbohydrates * productGi / 100f)
        needRecalculation = false
    }

    fun setProduct(newProduct: ProductFeatures) {
        productFeatures = ProductFeatures(newProduct)
        needRecalculation = true
    }

    fun setFactors(newFs: Factors?) {
        factors = newFs?.let { Factors(it) }!!
        needRecalculation = true
    }

    fun setDPS(newDps: DPS?) {
        dps = newDps?.let { DPS(it) }!!
        needRecalculation = true
    }

    fun getSlowDose(): Float {
        if (needRecalculation) calculateDoses()
        return slowDose
    }

    fun getCarbohydratesSlowDose(): Float {
        if (needRecalculation) calculateDoses()
        return carbohydratesSlowDose
    }

    fun getCarbohydratesFastDose(): Float {
        if (needRecalculation) calculateDoses()
        return carbohydratesFastDose
    }

    fun getCalories(): Float {
        if (needRecalculation) calculateDoses()
        return calories
    }

    val wholeDose: Float
        get() {
            if (needRecalculation) calculateDoses()
            return slowDose + carbohydratesSlowDose + carbohydratesFastDose + dps.dPSDose
        }

    val dPSDose: Float
        get() = dps.dPSDose

    companion object {
        const val PROTEINS = 4.0f
        const val FATS = 9.0f
        const val CARBOHYDRATES = 4.0f
    }
}