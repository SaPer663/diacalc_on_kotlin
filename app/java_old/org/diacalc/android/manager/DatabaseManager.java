package org.diacalc.android.manager;

import java.util.ArrayList;

import org.diacalc.android.maths.Factors;
import org.diacalc.android.maths.User;
import org.diacalc.android.products.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseManager extends SQLiteOpenHelper {
	public static final String dbName = "dcjmobile";
	public static final int dbVersion = 3;
	
	public static final String userTable = "user";
	public static final String groupTable = "prodgr";
	public static final String prodsTable = "products";
	public static final String menuTable = "menu";
	
	private static final String PROD_ID = "idProd";
	private static final String PROD_NAME = "name";
	private static final String PROD_PROT = "prot";
	private static final String PROD_FAT = "fat";
	private static final String PROD_CARB = "carb";
	private static final String PROD_GI = "gi";
	private static final String PROD_WEIGHT = "weight";
	private static final String PROD_MOBILE = "mobile";
	private static final String PROD_OWNER = "owner";
	private static final String PROD_USAGE = "usage";
	
	private static final String MENU_SNACK = "isSnack";
	
	private static final String USER_ID = "idUser";
	private static final String USER_LOGIN = "login";
	private static final String USER_PASS = "pass";
	private static final String USER_SERVER = "server";
	private static final String USER_MMOL = "mmol";
	private static final String USER_PLASMA = "plasma";
	private static final String USER_TARGET = "targetSh";
	private static final String USER_LOW = "lowSugar";
	private static final String USER_HI = "hiSugar";
	private static final String USER_ROUND = "round";
	private static final String USER_BE = "BE";
	private static final String USER_K1 = "k1";
	private static final String USER_K2 = "k2";
	private static final String USER_K3 = "k3";
	private static final String USER_S1 = "sh1";
	private static final String USER_S2 = "sh2";
	private static final String USER_TIME_SENSE = "timeSense";
	private static final String USER_MENU_INFO = "menuInfo";
	
	
	private static final String GROUP_ID = "idGroup";
	private static final String GROUP_NAME = "name";
	private static final String GROUP_SORT_INDX = "sortIndx";
	
	public DatabaseManager(Context context){
		super(context, dbName, null,dbVersion);
	}
	public void onCreate(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS "+userTable);
		db.execSQL("DROP TABLE IF EXISTS "+prodsTable);
		db.execSQL("DROP TABLE IF EXISTS "+groupTable);
		db.execSQL("DROP TABLE IF EXISTS "+menuTable);
		
		db.execSQL("CREATE TABLE "+userTable+" ("+
				USER_ID + " INTEGER PRIMARY KEY NOT NULL, " +
				USER_LOGIN + " TEXT, " +
				USER_PASS + " TEXT, " +
				USER_SERVER + " TEXT, " +
				USER_MMOL + " INTEGER NOT NULL, " +
				USER_PLASMA + " INTEGER NOT NULL, " +
				USER_TARGET + " REAL NOT NULL, " +
				USER_LOW + " REAL, " + 
				USER_HI + " REAL, "+
				USER_ROUND + " INTEGER, " +
				USER_BE + " REAL NOT NULL, " +
				USER_K1 + " REAL NOT NULL, " +
				USER_K2 + " REAL NOT NULL, " +
				USER_K3 + " REAL NOT NULL, " +
				USER_S1 + " REAL NOT NULL, " +
				USER_S2 + " REAL NOT NULL, " +
				USER_TIME_SENSE + " INTEGER NOT NULL, "+
				USER_MENU_INFO + " INTEGER);");
		//db.execSQL() Тут нужно внести нового дефолтного пользователя
		User us = new User("noname","",User.DEF_SERVER,new Factors(),
				User.ROUND_1,User.WHOLE,User.MMOL,5.6f,5.6f,
				User.NO_TIMESENSE,5.6f,3.2f,7.8f,0);
		
		ContentValues cv=new ContentValues(16);
		cv.put(USER_LOGIN, us.getLogin());
		cv.put(USER_PASS,us.getPass());
		cv.put(USER_SERVER, us.getServer());
		cv.put(USER_K1, us.getFactors().getK1Value());
		cv.put(USER_K2, us.getFactors().getK2());
		cv.put(USER_K3, us.getFactors().getK3());
		cv.put(USER_BE, us.getFactors().getBEValue());
		cv.put(USER_ROUND, us.getRound());
		cv.put(USER_PLASMA,us.isPlasma()?1:0);//Если плазма, то пишем 1, иначе нуль
		cv.put(USER_MMOL, us.isMmol()?1:0);
		cv.put(USER_S1, us.getS1());
		cv.put(USER_S2, us.getS2());
		cv.put(USER_TIME_SENSE, us.isTimeSense()?1:0);
		cv.put(USER_TARGET, us.getTargetSugar());
		cv.put(USER_LOW, us.getLowSugar());
		cv.put(USER_HI, us.getHiSugar());
		cv.put(USER_MENU_INFO, us.getMenuInfo());
		
		db.insert(userTable, null, cv);
        
		//create groupTable
		db.execSQL("CREATE TABLE "+groupTable+" ("+
				   GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " +
				   GROUP_NAME + " TEXT NOT NULL, " +
				   GROUP_SORT_INDX + " INTEGER);");
		
		//create productsTable
		db.execSQL("CREATE TABLE "+prodsTable+" (" + 
				PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
				PROD_NAME +" TEXT NOT NULL, " +
				PROD_PROT + " REAL NOT NULL, " +
				PROD_FAT + " REAL NOT NULL, " +
				PROD_CARB + " REAL NOT NULL, " +
				PROD_GI + " INTEGER NOT NULL, " +
				PROD_WEIGHT + " REAL NOT NULL, " +
				PROD_MOBILE + " INTEGER NOT NULL, " +
				PROD_OWNER + " INTEGER NOT NULL, " +
				PROD_USAGE + " INTEGER  NOT NULL  DEFAULT (0));");
		
		//create menu table
		db.execSQL("CREATE TABLE "+menuTable+ " ("+
				PROD_ID + " INTEGER PRIMARY KEY NOT NULL, " +  //Индекс
				PROD_NAME + " TEXT NOT NULL, " +
				PROD_PROT + " REAL NOT NULL, " +
				PROD_FAT + " REAL NOT NULL, " +
				PROD_CARB + " REAL NOT NULL, " +
				PROD_GI + " INTEGER NOT NULL, " +
				PROD_WEIGHT + " REAL, " + 
				MENU_SNACK + " INTEGER);");//перекус ли, оставляем на будущее
		
		//db.close();
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	//do nothing yet	
		db.execSQL("DROP TABLE  IF EXISTS "+userTable);
		db.execSQL("DROP TABLE  IF EXISTS "+groupTable);
		db.execSQL("DROP TABLE  IF EXISTS "+prodsTable);
		db.execSQL("DROP TABLE  IF EXISTS "+menuTable);
		onCreate(db);
	}
	
	public ArrayList<ProductInMenu> getMenuProducts(){
		SQLiteDatabase db = getReadableDatabase();
		
		ArrayList<ProductInMenu> prods = new ArrayList<ProductInMenu>();
		Cursor c = 
			db.rawQuery("SELECT * FROM "+menuTable +" ORDER BY "+PROD_NAME, null);
		
		c.moveToFirst();
		
		if (c.getCount()>0)
			do{
				prods.add(
					new ProductInMenu(
						c.getString(c.getColumnIndexOrThrow(PROD_NAME)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_PROT)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_FAT)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_CARB)),
						c.getInt(c.getColumnIndexOrThrow(PROD_GI)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_WEIGHT)),
						-1 )
				);
			}while (c.moveToNext());
		c.close();
		db.close();
		
		return prods;
	}
	public ArrayList<ProductInBase> getProducts(){
		ArrayList<ProductInBase> prods = new ArrayList<ProductInBase>();
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor c = 
			db.rawQuery("SELECT * FROM "+prodsTable +" ORDER BY "+PROD_NAME, null);
		
		c.moveToFirst();
		
		if (c.getCount()>0)
			do{
				prods.add(
					new ProductInBase(
						c.getString(c.getColumnIndexOrThrow(PROD_NAME)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_PROT)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_FAT)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_CARB)),
						c.getInt(c.getColumnIndexOrThrow(PROD_GI)),
						c.getFloat(c.getColumnIndexOrThrow(PROD_WEIGHT)),
						c.getInt(c.getColumnIndexOrThrow(PROD_MOBILE))==1,
						c.getInt(c.getColumnIndex(PROD_OWNER)),
						c.getInt(c.getColumnIndex(PROD_USAGE)),
						c.getInt(c.getColumnIndex(PROD_ID)) )
				);
			}while (c.moveToNext());
		
		c.close();
		db.close();
		
		return prods;
	}
	public ArrayList<ProductGroup> getGroups(){
		ArrayList<ProductGroup> groups = new ArrayList<ProductGroup>();
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor c = 
			db.rawQuery("SELECT * FROM "+groupTable +" ORDER BY "+GROUP_SORT_INDX, null);
		
		c.moveToFirst();
		
		if (c.getCount()>0)
			do{
				groups.add(
					new ProductGroup(
						c.getString(c.getColumnIndexOrThrow(GROUP_NAME)),
						c.getInt(c.getColumnIndex(GROUP_ID)) )
				);
			}while (c.moveToNext());
		
		c.close();
		db.close();
		
		return groups;
	}
	public void putProducts(ArrayList<ProductGroup> gr, ArrayList<ProductInBase> pr){
		SQLiteDatabase db = this.getWritableDatabase();
		//Чистим
		db.delete(groupTable, null, null);
		db.delete(prodsTable, null, null);
		for(int g=0;g<gr.size();g++){
			ContentValues cv_g = new ContentValues(2);
			cv_g.put(GROUP_NAME, gr.get(g).getName());
			cv_g.put(GROUP_SORT_INDX, g+1);
			
			long g_id = db.insert(groupTable, null, cv_g);
			Log.i("sqlite", ""+g_id);
			db.beginTransaction();
			try {
				for(int p=0;p<pr.size();p++){
					if(pr.get(p).getOwner()==gr.get(g).getId()){
						ContentValues cv_p=new ContentValues(7);
						cv_p.put(PROD_NAME, pr.get(p).getName());
						cv_p.put(PROD_PROT, pr.get(p).getProt());
						cv_p.put(PROD_FAT, pr.get(p).getFat());
						cv_p.put(PROD_CARB, pr.get(p).getCarb());
						cv_p.put(PROD_GI, pr.get(p).getGi());
						cv_p.put(PROD_WEIGHT, pr.get(p).getWeight());
						cv_p.put(PROD_MOBILE, pr.get(p).isMobile()?1:0);
						cv_p.put(PROD_OWNER, g_id);
						cv_p.put(PROD_USAGE, pr.get(p).getUsage());
						
					    db.insert(prodsTable, null, cv_p);
					}
				}
				db.setTransactionSuccessful();
			}
			catch (SQLException e) {}
			finally {
			    db.endTransaction();
			}

		}
		db.close();
	}
	public void putMenuProds(ArrayList<ProductInMenu> prods){
		SQLiteDatabase db = this.getWritableDatabase();
		//Сначала очищаем таблицу
		//db.rawQuery("DELETE FROM "+menuTable, null);
		db.delete(menuTable, null, null);
		
		db.beginTransaction();
		try{
			for(int i=0;i<prods.size();i++){
			//    db.insert(SOME_TABLE, null, SOME_VALUE);
				ContentValues cv=new ContentValues(7);
				cv.put(PROD_NAME, prods.get(i).getName());
				cv.put(PROD_PROT, prods.get(i).getProt());
				cv.put(PROD_FAT, prods.get(i).getFat());
				cv.put(PROD_CARB, prods.get(i).getCarb());
				cv.put(PROD_GI, prods.get(i).getGi());
				cv.put(PROD_WEIGHT, prods.get(i).getWeight());
				cv.put(MENU_SNACK, 0);
				
			    db.insert(menuTable, null, cv);
			}
			db.setTransactionSuccessful();
		}
		catch (SQLException e) {} 
		finally {
			db.endTransaction();
		}
		db.close();
	}
	public void deleteProduct(ProductInBase p){
		SQLiteDatabase db = getWritableDatabase();
		db.delete(prodsTable, PROD_ID+"=?", new String []{""+p.getId()});
		db.close();
	}
	public void insertProduct(ProductInBase prod){
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues cv=new ContentValues(9);
		cv.put(PROD_NAME, 	prod.getName());
		cv.put(PROD_PROT, 	prod.getProt());
		cv.put(PROD_FAT, 	prod.getFat());
		cv.put(PROD_CARB, 	prod.getCarb());
		cv.put(PROD_GI, 	prod.getGi());
		cv.put(PROD_WEIGHT, prod.getWeight());
		cv.put(PROD_MOBILE, 1);
		cv.put(PROD_OWNER, prod.getOwner());
		cv.put(PROD_USAGE, 0);
		
		prod.setId((int)db.insert(prodsTable, null, cv));
		
		db.close();
	}
	public void changeProduct(ProductInBase prod){
		SQLiteDatabase db = getWritableDatabase();
		
		ContentValues cv=new ContentValues(9);
		cv.put(PROD_NAME, 	prod.getName());
		cv.put(PROD_PROT, 	prod.getProt());
		cv.put(PROD_FAT, 	prod.getFat());
		cv.put(PROD_CARB, 	prod.getCarb());
		cv.put(PROD_GI, 	prod.getGi());
		cv.put(PROD_WEIGHT, prod.getWeight());
		cv.put(PROD_MOBILE, prod.isMobile()?1:0);
		cv.put(PROD_OWNER, prod.getOwner());
		cv.put(PROD_USAGE, prod.getUsage());
		
		db.update(prodsTable, cv, PROD_ID+"=?", new String[]{""+prod.getId()});
		
		db.close();
	}
	public User getUser(){
		SQLiteDatabase db = getReadableDatabase();
		
		Cursor c = 
			db.rawQuery("SELECT * FROM "+userTable, null);
		
		User user = null;
		
		c.moveToFirst();
		
		if (c.getCount()>0){
			user = new User(
					c.getString(c.getColumnIndex(USER_LOGIN)),
					c.getString(c.getColumnIndex(USER_PASS)),
					c.getString(c.getColumnIndex(USER_SERVER)),
					new Factors(
						c.getFloat(c.getColumnIndex(USER_K1)),
						c.getFloat(c.getColumnIndex(USER_K2)),
						c.getFloat(c.getColumnIndex(USER_K3)),
						c.getFloat(c.getColumnIndex(USER_BE)),
						Factors.DIRECT
						),
					c.getInt(c.getColumnIndex(USER_ROUND)),
					c.getInt(c.getColumnIndex(USER_PLASMA))==1,
					c.getInt(c.getColumnIndex(USER_MMOL))==1,
					c.getFloat(c.getColumnIndex(USER_S1)),
					c.getFloat(c.getColumnIndex(USER_S2)),
					c.getInt(c.getColumnIndex(USER_TIME_SENSE))==1,
					c.getFloat(c.getColumnIndex(USER_TARGET)),
					c.getFloat(c.getColumnIndex(USER_LOW)),
					c.getFloat(c.getColumnIndex(USER_HI)),
					c.getInt(c.getColumnIndex(USER_MENU_INFO))
					);
		}else{
			user =  new User("noname","",User.DEF_SERVER,new Factors(),
					User.ROUND_1,User.WHOLE,User.MMOL,5.6f,5.6f,
					User.NO_TIMESENSE,5.6f,3.2f,7.8f,0);
		}
		
		c.close();
		
		db.close();
		
		return user;
	}
	public void putUser(User us){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues cv=new ContentValues(16);
		cv.put(USER_LOGIN, us.getLogin());
		cv.put(USER_PASS,us.getPass());
		cv.put(USER_SERVER, us.getServer());
		cv.put(USER_K1, us.getFactors().getK1Value());
		cv.put(USER_K2, us.getFactors().getK2());
		cv.put(USER_K3, us.getFactors().getK3());
		cv.put(USER_BE, us.getFactors().getBEValue());
		cv.put(USER_ROUND, us.getRound());
		cv.put(USER_PLASMA,us.isPlasma()?1:0);//Если плазма, то пишем 1, иначе нуль
		cv.put(USER_MMOL, us.isMmol()?1:0);
		cv.put(USER_S1, us.getS1());
		cv.put(USER_S2, us.getS2());
		cv.put(USER_TIME_SENSE, us.isTimeSense()?1:0);
		cv.put(USER_TARGET, us.getTargetSugar());
		cv.put(USER_LOW, us.getLowSugar());
		cv.put(USER_HI, us.getHiSugar());
		cv.put(USER_MENU_INFO, us.getMenuInfo());
		
		db.update(userTable, cv, null, null);
		
		db.close();
	}
}
