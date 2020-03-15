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
open class ProductW {

    var name: String = String()
    var prot: Float = 0f
    var fat: Float = 0f
    var carb: Float = 0f
    internal var gi: Int = 50
    internal var weight: Float = 0f

    constructor()
    constructor(newName: String?, newProt: Float, newFat: Float, newCarb: Float,
                newGi: Int, newWeight: Float) {
        if (newName != null) {
            name = newName
        }
        prot = newProt
        fat = newFat
        carb = newCarb
        setGi(newGi)
        setWeight(newWeight)
    }

    constructor(newProd: ProductW?) {
        if (newProd != null) {
            name = newProd.name
        }
        if (newProd != null) {
            prot = newProd.prot
        }
        if (newProd != null) {
            fat = newProd.fat
        }
        if (newProd != null) {
            carb = newProd.carb
        }
        if (newProd != null) {
            gi = newProd.getGi()
        }
        if (newProd != null) {
            weight = newProd.getWeight()
        }
    }

    fun getGi(): Int = gi


    fun getWeight(): Float = weight


    val calories: Float
        get() = (Dose.PROT * allProt) + (Dose.FAT * allFat) + (
                Dose.CARB * allCarb)



    private fun flush() {
        name = ""
        prot = 0f
        fat = 0f
        carb = 0f
        gi = 0
        weight = 0f
    }

    private fun setGi(newGi: Int) {
        gi = if (newGi < 0) 50 else if (newGi > 100) 100 else if (newGi == 0) 50 else newGi
    }

    fun setWeight(newWeight: Float) {
        weight = if (newWeight < 0) 0f else newWeight
    }

    val allProt: Float
        get() {
            return prot * weight / 100f
        }

    val allFat: Float
        get() {
            return fat * weight / 100f
        }

    val allCarb: Float
        get() {
            return carb * weight / 100f
        }

    fun plusProd(ProdToAdd: ProductW) {
        val allProt: Float = prot * weight / 100f + ProdToAdd.allProt
        val allFat: Float = fat * weight / 100f + ProdToAdd.allFat
        val allCarb: Float = carb * weight / 100f + ProdToAdd.allCarb
        val allCarbQuick: Float = (gi / 100f) * carb * weight / 100f +
                (ProdToAdd.getGi() / 100f) * ProdToAdd.allCarb
        val newGi: Int
        newGi = if (allCarb == 0f) 50 else {
            (((100f * allCarbQuick / allCarb) + 0.5f) * 100f).toInt() / 100
            //Math.round(100f * AllCarbQuick / AllCarb);
        } //RoundTo(100 * AllCarbQuick / AllUg , 0 );
        /*
         // round to 2 decimal points
            double number = (double)(int)((bmi+0.005)*100.0)/100.0;
        */
        val newWeight: Float = weight + ProdToAdd.getWeight()
        if (newWeight != 0f) {
            if (name.isEmpty()) name = ProdToAdd.name else name += " " + ProdToAdd.name
            prot = 100f * allProt / newWeight
            fat = 100f * allFat / newWeight
            carb = 100f * allCarb / newWeight
            setGi(newGi)
            weight = newWeight
        } else flush()
    }

    fun equals(pr: ProductW): Boolean {
        return (name == pr.name) && (prot == pr.prot) && (fat == pr.fat) && (
                carb == pr.carb) && (gi == pr.gi) && (weight == pr.weight)
    }

    override fun toString(): String {
        return "$name $prot $fat $carb $gi"
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