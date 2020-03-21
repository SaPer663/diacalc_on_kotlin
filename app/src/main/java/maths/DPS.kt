/*
 * DO  NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
</www.diacalc.org> */
class DPS {
    private  var factors: Factors = Factors()
    private var highBloodSugar: Sugar = Sugar()
    private var bloodSugarTargets: Sugar = Sugar()

    constructor() {
        highBloodSugar = Sugar()
        bloodSugarTargets = Sugar()
        factors = Factors()
    }

    constructor(newHighValueSugar: Sugar, newTargetValueSugar: Sugar, newFactors: Factors) {
        highBloodSugar = if (newHighValueSugar.value > 0) Sugar(newHighValueSugar) else Sugar()
        bloodSugarTargets = if (newTargetValueSugar.value > 0) Sugar(newTargetValueSugar) else Sugar()
        factors = Factors(newFactors)
    }

    constructor(newDPS: DPS) {
        highBloodSugar = Sugar(newDPS.highBloodSugar)
        bloodSugarTargets = Sugar(newDPS.bloodSugarTargets)
        factors = Factors(newDPS.getFactors())
    }


    private fun getFactors(): Factors {
        return factors
    }

    val dPSDose: Float
        get() = (if (factors.unitCostOfInsulin < 0.01f) 0f
                 else (highBloodSugar.value - bloodSugarTargets.value) / (factors.unitCostOfInsulin))
}

/*
constructor(newDps: Unit)   не используемые функции

    fun setSh1(newSh1: Sugar) {
        highValueSugar = if (newSh1.value > 0) Sugar(newSh1) else Sugar()
    }

    fun setSh2(newSh2: Sugar) {
        targetValueSugar = if (newSh2.value > 0) Sugar(newSh2) else Sugar()
    }

    fun setFs(newFs: Factors) {
        factors = Factors(newFs)
    }

    fun getSh1(): Sugar? {
        return highValueSugar
    }

    fun getSh2(): Sugar? {
        return targetValueSugar
    }
 */