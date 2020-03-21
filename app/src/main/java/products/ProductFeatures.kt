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
package org.diacalc.android.products

import org.diacalc.android.maths.Dose

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
</www.diacalc.org> */
open class ProductFeatures {

    var name: String = String()
    var proteins: Float = 0f
    var fats: Float = 0f
    var carbohydrates: Float = 0f
    internal var gi: Float = 50f
    internal var weight: Float = 0f

    constructor()
    constructor(newName: String, newProteins: Float, newFats: Float, newCarbohydrates: Float,
                newGi: Float, newWeight: Float) {
        name = newName
        proteins = newProteins
        fats = newFats
        carbohydrates = newCarbohydrates
        setGi(newGi)
        setWeight(newWeight)
    }

    constructor(newProductFeatures: ProductFeatures) {
        name = newProductFeatures.name
        proteins = newProductFeatures.proteins
        fats = newProductFeatures.fats
        carbohydrates = newProductFeatures.carbohydrates
        gi = newProductFeatures.getGi()
        weight = newProductFeatures.getWeight()
    }

    fun getGi(): Float = gi


    fun getWeight(): Float = weight


    val calories: Float
        get() = (Dose.PROTEINS * allProteins) + (Dose.FATS * allFats) + (
                Dose.CARBOHYDRATES * allCarbohydrates)



    private fun flush() {
        name = ""
        proteins = 0f
        fats = 0f
        carbohydrates = 0f
        gi = 0f
        weight = 0f
    }

    private fun setGi(newGi: Float) {
        gi = if (newGi <= 0) 50f else if (newGi > 100) 100f else newGi
    }

    fun setWeight(newWeight: Float) {
        weight = if (newWeight < 0) 0f else newWeight
    }

    val allProteins: Float
        get() {
            return proteins * weight / 100f
        }

    val allFats: Float
        get() {
            return fats * weight / 100f
        }

    val allCarbohydrates: Float
        get() {
            return carbohydrates * weight / 100f
        }

    fun productAddition(productBeingAdded: ProductFeatures) {
        val totalAmoutProteins: Float = proteins * weight / 100f + productBeingAdded.allProteins
        val totalAmountFats: Float = fats * weight / 100f + productBeingAdded.allFats
        val totalAmountCarbohydrates: Float = carbohydrates * weight / 100f + productBeingAdded.allCarbohydrates
        val totalFastCarbohydrates: Float = (gi / 100f) * carbohydrates * weight / 100f +
                (productBeingAdded.getGi() / 100f) * productBeingAdded.allCarbohydrates
        val newGi: Float = if (totalAmountCarbohydrates == 0f) 50f else {
            (((100f * totalFastCarbohydrates / totalAmountCarbohydrates) + 0.5f) * 100f) / 100
            //Math.round(100f * AllCarbQuick / AllCarb);
        } //RoundTo(100 * AllCarbQuick / AllUg , 0 );
        /*
         // round to 2 decimal points
            double number = (double)(int)((bmi+0.005)*100.0)/100.0;
        */
        val newWeight: Float = weight + productBeingAdded.getWeight()
        if (newWeight != 0f) {
            if (name.isEmpty()) name = productBeingAdded.name else name += " $productBeingAdded.name"
            proteins = 100f * totalAmoutProteins / newWeight
            fats = 100f * totalAmountFats / newWeight
            carbohydrates = 100f * totalAmountCarbohydrates / newWeight
            setGi(newGi)
            weight = newWeight
        } else flush()
    }

    fun isEquals(pr: ProductFeatures): Boolean {
        return (name == pr.name) && (proteins == pr.proteins) && (fats == pr.fats) && (
                carbohydrates == pr.carbohydrates) && (gi == pr.gi) && (weight == pr.weight)
    }

    override fun toString(): String {
        return "$name $proteins $fats $carbohydrates $gi"
    }
}

/*
public void copyProduct(ProductW newProd){
        Name = newProd.getName();
        Prot = newProd.getProt();
        Fat = newProd.getFat();
        Carb = newProd.getCarb();
        Gi = newProd.getGi();
        Weight = newProd.getWeight();

val gL: Float
        get() {
            return carb * weight * gi / 10000f
        }
fun changeWeight(newWeight: Float) {
        if (newWeight > 0 && weight != 0f) {
            val kW: Float = weight / newWeight
            if ((prot * kW.also { prot = it }) > 100f) prot = 100f
            if ((fat * kW.also { fat = it }) > 100f) fat = 100f
            if ((carb * kW.also { carb = it }) > 100f) carb = 100f
        } else {
            prot = 0f
            fat = 0f
            carb = 0f
            gi = 0
        }
        setWeight(newWeight)
    }

 */