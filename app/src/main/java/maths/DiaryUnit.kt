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
class DiaryUnit {
    private var id: Int
    var time: Long
    var comment: String
    var sh1 = 0f
    private var sh2 = 0f
    var type: Int
        private set
    private var fcs: Factors? = null
    private var dose = 0f
    private var prod: ProductW? = null

    constructor(id: Int, time: Long, comment: String, sh: Float) { //create record about sugar
        this.id = id
        this.time = time
        this.comment = comment
        sh1 = sh
        type = SUGAR
        setId(id)
    }

    constructor(id: Int, time: Long, comment: String, sh1: Float, sh2: Float,
                fc: Factors?, dose: Float, prod: ProductW?) { //create record about meal
        this.id = id
        this.time = time
        this.comment = comment
        this.sh1 = sh1
        this.sh2 = sh2
        fcs = fc
        this.dose = dose
        type = MENU
        this.prod = prod
        setId(id)
    }

    constructor(id: Int, time: Long, comment: String) { //create just comment
        this.id = id
        this.time = time
        this.comment = comment
        type = COMMENT
        setId(id)
    }

    fun getId(): Int {
        return id
    }

    var product: ProductW?
        get() = if (type == SUGAR) null else prod
        set(prod) {
            this.prod = prod
        }

    var factors: Factors?
        get() = if (type == SUGAR) null else fcs
        set(v) {
            fcs = v
        }

    fun getSh2(): Float {
        return if (type == SUGAR) 0f else sh2
    }

    fun getDose(): Float {
        return if (type == SUGAR) 0f else dose
    }

    fun setId(v: Int) {
        if (v >= 0) {
            id = v
            maxid = java.lang.Math.max(maxid, v)
        } else {
            id = ++maxid
        }
    }

    fun setSh2(v: Float) {
        sh2 = v
    }

    fun setDose(v: Float) {
        dose = v
    }

    companion object {
        const val MENU = 0
        const val SUGAR = 1
        const val COMMENT = 2
        private var maxid = 0
    }
}