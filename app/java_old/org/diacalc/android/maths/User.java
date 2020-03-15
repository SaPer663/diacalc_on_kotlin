package org.diacalc.android.maths;

public class User {
    public final static int ROUND_1 = 1;
    public final static int ROUND_05 = 2;

    public final static int PFC_INFO = 0;
    public final static int BE_INFO = 1;
    public final static int CALOR_INFO = 2;
    public final static int DOSE_INFO = 3;
    
    public final static String DEF_SERVER = "http://diacalc.org/dbwork/";
    
    public final static boolean PLASMA = true;
    public final static boolean WHOLE = false;
    
    public final static boolean MMOL = true;
    public final static boolean MGDL = false;
    
    public final static boolean TIMESENSE = true;
    public final static boolean NO_TIMESENSE = false;

    private String login;
    private String pass;
    private String server;
    private Factors fcs;
    private int round;
        
    private boolean mmol;
    private boolean plasma;
    private float s1,s2;//sugars to restore after timer
    private boolean timeSense;
    private float targetS,lowS,hiS;//sugars target, low level, hi level
    private int menuInfo;
    
    public User(String login,String pass,String server,Factors fcs,
            int round,boolean plasma,boolean mmol,
            float s1,float s2,boolean timeSense,float target,
            float low,float hi,int menuInfo){
        this.login = login;
        this.pass = pass;
        this.server = server;
        this.fcs = fcs;
        this.round = round;
        this.plasma = plasma;
        this.mmol = mmol;
        this.s1 = s1;
        this.s2 = s2;
        this.timeSense = timeSense;
        this.targetS = target;
        this.lowS = low;
        this.hiS = hi;
        this.menuInfo = menuInfo;
    }
    public User(User user){
    	login = user.login;
        pass = user.pass;
        server = user.server;
        fcs = new Factors(user.fcs);
        round = user.round;
        plasma = user.plasma;
        mmol = user.mmol;
        s1 = user.s1;
        s2 = user.s2;
        timeSense = user.timeSense;
        targetS = user.targetS;
        lowS = user.lowS;
        hiS = user.hiS;
        menuInfo = user.menuInfo;
    }
    public int getMenuInfo(){
    	return menuInfo;
    }
    public void setMenuInfo(int v) {
		menuInfo = v;
	}
    public void setTargetSugar(float v){
        targetS = v;
    }
    public void setLowSugar(float v){
        lowS = v;
    }
    public void setHiSugar(float v){
        hiS = v;
    }
    public float getTargetSugar(){
        return targetS;
    }
    public float getLowSugar(){
        return lowS;
    }
    public float getHiSugar(){
        return hiS;
    }
    public void setTimeSense(boolean v){
        timeSense = v;
    }
    public boolean isTimeSense(){
        return timeSense;
    }
    public void setS1(float v){
        s1 = v;
    }
    public void setS2(float v){
        s2 = v;
    }
    public float getS1(){
        return s1;
    }
    public float getS2(){
        return s2;
    }
    public void setPlasma(boolean v){
        plasma = v;
    }
    public void setMmol(boolean v){
        mmol = v;
    }
    public boolean isPlasma(){
        return plasma;
    }
    public boolean isMmol(){
        return mmol;
    }
    public void setRound(int v){
        round = v;
    }
    public int getRound(){
        return round;
    }
    
    public void setLogin(String v){
        login = v;
    }
    public void setPass(String v){
        pass = v;
    }
    public void setServer(String v){
        server = v;
    }
    public void setFactors(Factors v){
        fcs = v;
    }

    public String getLogin(){
        return login;
    }
    public String getPass(){
        return pass;
    }
    public String getServer(){
        return server;
    }
    public Factors getFactors(){
        return fcs;
    }
}
