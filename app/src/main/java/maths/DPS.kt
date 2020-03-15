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

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
</www.diacalc.org> */
class DPS {
    private lateinit var fs: Factors
    private var sh1: Sugar? = null
    private var sh2: Sugar? = null

    constructor() {
        sh1 = Sugar()
        sh2 = Sugar()
        fs = Factors()
    }

    constructor(newSh1: Sugar, newSh2: Sugar, newFs: Factors?) {
        sh1 = if (newSh1.value > 0) Sugar(newSh1) else Sugar()
        sh2 = if (newSh2.value > 0) Sugar(newSh2) else Sugar()
        fs = newFs?.let { Factors(it) }!!
    }

    constructor(newDps: DPS) {
        sh1 = newDps.sh1?.let { Sugar(it) }
        sh2 = newDps.sh2?.let { Sugar(it) }
        fs = Factors(newDps.getFs())
    }

    constructor(newDps: Unit)

    fun setSh1(newSh1: Sugar) {
        sh1 = if (newSh1.value > 0) Sugar(newSh1) else Sugar()
    }

    fun setSh2(newSh2: Sugar) {
        sh2 = if (newSh2.value > 0) Sugar(newSh2) else Sugar()
    }

    fun setFs(newFs: Factors?) {
        fs = newFs?.let { Factors(it) }!!
    }

    fun getSh1(): Sugar? {
        return sh1
    }

    fun getSh2(): Sugar? {
        return sh2
    }

    private fun getFs(): Factors {
        return fs
    }

    val dPSDose: Float
        get() = if (fs.k3 < 0.01f) 0f else (sh1!!.value - sh2!!.value) / fs.k3
}