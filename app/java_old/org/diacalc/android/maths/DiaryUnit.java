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

package org.diacalc.android.maths;

/**
 *
 * @author Toporov Konstantin <www.diacalc.org>
 */

import org.diacalc.android.products.ProductW;

public class DiaryUnit {
    public final static int MENU = 0;
    public final static int SUGAR = 1;
    public final static int COMMENT = 2;
    private int id;
    private long time;
    private String comment;
    private float sh1;
    private float sh2;
    private int type;
    private static int maxid = 0;
    private Factors fcs;
    private float dose;
    private ProductW prod;

    public DiaryUnit(int id, long time, String comment, float sh){//create record about sugar
        this.id = id;
        this.time = time;
        this.comment = comment;
        sh1 = sh;
        type = SUGAR;
        setId(id);
    }

    public DiaryUnit(int id,long time, String comment, float sh1, float sh2,
            Factors fc, float dose, ProductW prod){//create record about meal
        this.id = id;
        this.time = time;
        this.comment = comment;
        this.sh1 = sh1;
        this.sh2 = sh2;
        fcs = fc;
        this.dose = dose;

        type = MENU;
        
        this.prod = prod;

        setId(id);
    }
    public DiaryUnit(int id, long time, String comment){//create just comment
        this.id = id;
        this.time = time;
        this.comment = comment;
        type = COMMENT;

        setId(id);
    }

    public int getId(){
        return id;
    }
    public String getComment(){
        return comment;
    }
    public int getType(){
        return type;
    }
    public ProductW getProduct(){
        return type==SUGAR? null : prod;
    }
    public Factors getFactors(){
        return type==SUGAR? null : fcs;
    }
    public float getSh1(){
        return sh1;
    }
    public float getSh2(){
        return type==SUGAR? 0 : sh2;
    }
    public float getDose(){
        return type==SUGAR? 0 : dose;
    }
    public long getTime(){
        return time;
    }

    public void setProduct(ProductW prod){
        this.prod = prod;
    }
    public void setTime(long v){
        time = v;
    }
    public void setId(int v){
        if (v>=0){
            this.id = v;
            maxid = Math.max(maxid, v);
        }
        else{
            id = ++maxid;
        }
    }
    public void setComment(String v){
        comment = v;
    }
    public void setFactors(Factors v){
        fcs = v;
    }
    public void setSh1(float v){
        sh1 = v;
    }
    public void setSh2(float v){
        sh2 = v;
    }
    public void setDose(float v){
        dose = v;
    }
}
