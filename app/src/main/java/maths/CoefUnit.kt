/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.diacalc.android.maths

/**
 *
 * @author connie
 */
class CoefUnit {
    private  var fcs: Factors
    var time: Int //in seconds


    /*private int id;
    private static int counter = 0;*/

    constructor(time: Int, fcs: Factors) {
        this.time = time
        this.fcs = Factors(fcs)
        //id = ++counter;
    }

    constructor(cu: CoefUnit) {
        time = cu.time
        fcs = Factors(cu.fcs)
    }

    constructor() {
        time = 8 * 60 * 60 //8 утра
        fcs = Factors()
        //id = ++counter;
    }

    //Str.extendZero(time / (60*60)) + ":" +
    //Str.extendZero(time % (60*60) / 60 );
    val timeString: String
        get() = "" //Str.extendZero(time / (60*60)) + ":" +
    //Str.extendZero(time % (60*60) / 60 );

    /*public int getId(){
        return id;
    }*/


    var factors: Factors
        get() = fcs
        set(v) {
            fcs = Factors(v)
        }

}