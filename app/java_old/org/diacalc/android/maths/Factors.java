package org.diacalc.android.maths;

public class Factors {
    public final static boolean INDIRECT = true;
    public final static boolean DIRECT = false;
    private float k1,k2,k3,be;

public Factors(){
    k1 = 1f;
    k2 = 0f;
    k3 = 10f;
    be = 10f;
    }

private float convertK1toIndirect(float newK1, float newXE){
    if (newK1>0) return 10 * newXE/newK1;
    else return 0f;
}
public Factors(float newk1, float newk2, float newk3, float newXE, 
        boolean direction){
    if (direction) {
        k1 = newk1>=0 ? newk1 : 10f;
        k1 = convertK1toIndirect(k1,newXE>0?newXE:1);
    }
    else k1 = newk1 >= 0 ? newk1 : 1f;
    k2 = newk2 >=0 ? newk2 : 0f;
    k3 = newk3;
    //Тут пересчет не нужен, т.к. к1 все равно пересчитывается и приводится к 
    //значению хе 10 гр.
    be = direction ? 10f : (newXE > 0 ? newXE : 10f);
}
public Factors(float newk1,float newk2,float newk3,float newBE){
    k1 = newk1 >= 0 ? newk1 : 1f;
    k2 = newk2;
    k3 = newk3;
    be = newBE > 0 ? newBE : 10f;
}
public Factors(Factors newFs){
    k1 = newFs.getK1(Factors.DIRECT);
    k2 = newFs.getK2();
    k3 = newFs.getK3();
    be = newFs.getBE(Factors.DIRECT);
}

public float getK1Value(){
    return k1;
}

public void setK1Value(float v){
    k1 = v >= 0 ? v : 1f;
}

public float getBEValue(){
    return be;
}

public void setBEValue(float v){
    be = v > 0 ? v : 10f;
}

public float getK1(boolean direction){
    if (direction) 
        if (k1>0) return be/k1;
        else return 0f;
    else return k1;
}
public float getK2(){
    return k2;
}
public float getK3(){
    return k3;
}
public float getBE(boolean direction){
    if (direction) return 1;
    return be;
}

public void setK1XE(float newk1,float newXE, boolean direction){
    if (direction){
        k1 = convertK1toIndirect(newk1>=0?newk1:10f,newXE>0?newXE:1f);
        be = 10f;
    }
    else{
        k1 = newk1 >= 0 ? newk1 : 1f;
        be = newXE > 0 ? newXE : 10f;
    }
    
}
public void setK2(float newk2){
    k2 = newk2 >=0 ? newk2 : 0f;
}
public void setK3(float newk3){
    k3 = newk3;
}

}
