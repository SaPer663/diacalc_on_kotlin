/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.diacalc.android.maths;

/**
 *
 * @author connie
 */
public class CoefUnit {
    private Factors fcs;
    private int time;//in seconds
    /*private int id;
    private static int counter = 0;*/

    public CoefUnit(int time,Factors fcs){
        this.time = time;
        this.fcs = new Factors(fcs);
        //id = ++counter;
    }
    public CoefUnit(CoefUnit cu){
        this.time = cu.time;
        this.fcs = new Factors(cu.fcs);
    }
    public CoefUnit(){
        this.time = 8*60*60; //8 утра
        this.fcs = new Factors();
        //id = ++counter;
    }
    public int getTime(){
        return time;
    }
    public String getTimeString(){
        return "";//Str.extendZero(time / (60*60)) + ":" +
                //Str.extendZero(time % (60*60) / 60 );
    }
    /*public int getId(){
        return id;
    }*/
    public Factors getFactors(){
        return fcs;
    }
    public void setTime(int v){
        time = v;
    }
    public void setFactors(Factors v){
        this.fcs = new Factors(v);
    }
}
