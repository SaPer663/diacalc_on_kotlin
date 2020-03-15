package org.diacalc.android.internet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.diacalc.android.maths.User;
import org.diacalc.android.products.ProductGroup;
import org.diacalc.android.products.ProductInBase;
import org.diacalc.android.products.ProductInMenu;

import android.util.Log;

//А с помощью этого класса будем делать запросы и получать ответы
public class DoingPost{
	public final static String ERROR = "error";
	
	public final static String MENU_MENU  = "menu content";
	public final static String MENU_SNACK  = "snack content";
	public final static String MENU_USER = "menu user";
	
	public final static String PRODS_GROUP = "group content";
	public final static String PRODS_PRODS = "products content";
	
	
	private final static String ACT_GET_MENU = "get menu";
	private final static String ACT_SEND_MENU = "send menu";
	
	private final static String ACT_GET_PRODS = "get mobile base";
	
	private User user;
	private String response;
	private boolean error = false;
	private String errMsg="";
	
	public DoingPost(User u){
		user = u;
	}
	
	private void doPost(List<NameValuePair> nameValuePairs){
		Log.i("RESPONSE","start");
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(user.getServer()+"server.php");
	    
	    try {
	        // Add your data
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"utf-8"));

	        // Execute HTTP Post Request
	        Log.i("RESPONSE","prepare");
	        HttpResponse responsePOST = httpclient.execute(httppost);
	        Log.i("RESPONSE","done");
	        HttpEntity resEntity = responsePOST.getEntity();
	        if (responsePOST.getStatusLine().getStatusCode()==200){
	        	if (resEntity != null) {    
	            	response = EntityUtils.toString(resEntity);
	            	if (!response.endsWith("<ok>")){
	            		error = true;
		            	errMsg = "error's happend\n"+response;
	            	}
	            }else{
	            	error = true;
	            	errMsg = "no response";
	            }
	        }
	        else{
	        	errMsg += "Wrong answer "+responsePOST.getStatusLine().getStatusCode();
	        	error = true;
	        }
	    } catch (ClientProtocolException e) {
	        error = true;
	    	errMsg += e.getMessage();
	    } catch (IOException e) {
	    	error = true;
	    	errMsg += e.getMessage();
	    } catch (Exception e){
	    	error = true;
	    	errMsg += e.getMessage();
	    }
	    
	}
	public HashMap<String,Object> sendMenu(ArrayList<ProductInMenu> prods){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("login", user.getLogin()));
        nameValuePairs.add(new BasicNameValuePair("pass", user.getPass()));
        nameValuePairs.add(new BasicNameValuePair("action", ACT_SEND_MENU));
        
        nameValuePairs.add(new BasicNameValuePair("k1", ""+
        		user.getFactors().getK1Value()*10/user.getFactors().getBEValue()));
        nameValuePairs.add(new BasicNameValuePair("k2", ""+user.getFactors().getK2()) );
        nameValuePairs.add(new BasicNameValuePair("k3", ""+user.getFactors().getK3()) );
        nameValuePairs.add(new BasicNameValuePair("sh1", ""+user.getS1() ));
        nameValuePairs.add(new BasicNameValuePair("sh2", ""+user.getS2() ));
        
        for(int i=0;i<prods.size();i++){
        	nameValuePairs.add(new BasicNameValuePair("names[]",prods.get(i).getName() ));
        	nameValuePairs.add(new BasicNameValuePair("prots[]",""+prods.get(i).getProt() ));
        	nameValuePairs.add(new BasicNameValuePair("fats[]",""+prods.get(i).getFat() ));
        	nameValuePairs.add(new BasicNameValuePair("carbs[]",""+prods.get(i).getCarb() ));
        	nameValuePairs.add(new BasicNameValuePair("gis[]",""+prods.get(i).getGi() ));
        	nameValuePairs.add(new BasicNameValuePair("weights[]",""+prods.get(i).getWeight() ));
        	nameValuePairs.add(new BasicNameValuePair("issnacks[]","0" ));
        }
        
        nameValuePairs.add(new BasicNameValuePair("ok", "ok"));
        
        doPost(nameValuePairs);
        
        HashMap<String, Object> hm = new HashMap<String, Object>();
        if (error){
        	hm.put(ERROR, errMsg);
        	return hm;
        }
        
        hm.put(ERROR, null);
    	return hm;
    }
	public HashMap<String,Object> requestProducts(){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("login", user.getLogin()));
        nameValuePairs.add(new BasicNameValuePair("pass", user.getPass()));
        nameValuePairs.add(new BasicNameValuePair("action", ACT_GET_PRODS));
        nameValuePairs.add(new BasicNameValuePair("ok", "ok"));
        
        doPost(nameValuePairs);
        
        HashMap<String, Object> hm = new HashMap<String, Object>();
        if (error){
        	hm.put(ERROR, errMsg);
        	return hm;
        }
        Log.i(DoingPost.class.getName(), response);
        //Теперь парсим вывод сервера
        String groupsS = null;
        try{
        	groupsS = getFirstTagged(response, "groups");
        }catch (Exception e){
        	hm.put(ERROR, e.getMessage());
        	return hm;
        }
        Log.i(DoingPost.class.getName(), "groups="+groupsS);
        
        String prodsS = null;
        try{
        	prodsS = getFirstTagged(response, "prods");
        }catch (Exception e){
        	hm.put(ERROR, e.getMessage());
        	return hm;
        }
        Log.i(DoingPost.class.getName(), "prods="+prodsS);
        
        ArrayList<ProductGroup> groups = parseGroups(groupsS);
        ArrayList<ProductInBase> prods = parseProds(prodsS);
        
        
        hm.put(PRODS_GROUP, groups);
        hm.put(PRODS_PRODS, prods);
        
        hm.put(ERROR, null);
        
        return hm;
	}
	private ArrayList<ProductGroup> parseGroups(String st){
		ArrayList<ProductGroup> arr = new ArrayList<ProductGroup>();
        String [] lines = st.trim().split("<br>");
        for(int i=0;i<lines.length;i++){
          if (lines[i].trim().length()>0){
            String [] param = lines[i].trim().split(" ");
            int id;
            if (param.length>1){
                try{
                    id = Integer.parseInt(param[param.length-1]);
                }catch(Exception ex){
                    id = 0;
                }
                String name ="";
                for(int j=0;j<(param.length-1);j++){
                    name += param[j] + " ";
                }
                name = name.trim();
                arr.add(new ProductGroup(name,id));
            }
          }
        }
		return arr;
	}
	private ArrayList<ProductInBase> parseProds(String st){
		ArrayList<ProductInBase> arr = new ArrayList<ProductInBase>();
        String [] lines = st.trim().split("<br>");
        for(int i=0;i<lines.length;i++){
          if (lines[i].trim().length()>0){
            String [] param = lines[i].trim().split(" "); 
            int gi,owner,usage;
            float c,f,p;
            if (param.length>6){
                try{
                    owner = Integer.parseInt(param[param.length-1]);
                    usage = Integer.parseInt(param[param.length-2]);
                    gi = Integer.parseInt(param[param.length-3]);
                    c = Float.parseFloat(param[param.length-4]);
                    f = Float.parseFloat(param[param.length-5]);
                    p = Float.parseFloat(param[param.length-6]);
                }catch(Exception ex){
                    //ex.printStackTrace();
                    owner = usage = gi = 0;
                    c = f = p = 0f;
                }
                String name ="";
                for(int j=0;j<(param.length-6);j++){
                    name += param[j] + " ";
                }
                name = name.trim();
                arr.add(new ProductInBase(name,p,f,c,gi,100f,false,owner,usage,-1));
            }
          }
        }
        return arr;
	}
	public HashMap<String,Object> requestMenu(){
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
        nameValuePairs.add(new BasicNameValuePair("login", user.getLogin()));
        nameValuePairs.add(new BasicNameValuePair("pass", user.getPass()));
        nameValuePairs.add(new BasicNameValuePair("action", ACT_GET_MENU));
        nameValuePairs.add(new BasicNameValuePair("ok", "ok"));
        
        doPost(nameValuePairs);
        
        HashMap<String, Object> hm = new HashMap<String, Object>();
        if (error){
        	hm.put(ERROR, errMsg);
        	return hm;
        }
        Log.i(DoingPost.class.getName(), response);
        //Теперь парсим вывод сервера
        String menu = null;
        try{
        	menu = getFirstTagged(response, "menu");
        }catch (Exception e){
        	hm.put(ERROR, e.getMessage());
        	return hm;
        }
        Log.i(DoingPost.class.getName(), "menu="+menu);
        String coefs = null;
        try{
        	coefs = getFirstTagged(response, "coefs");
        }catch (Exception e){
        	hm.put(ERROR, e.getMessage());
        	return hm;
        }
        Log.i(DoingPost.class.getName(), "coefs="+coefs);
        
        ArrayList<ProductInMenu> prods = parseMenu(menu,0);
        ArrayList<ProductInMenu> prodsSnack = parseMenu(menu,1);
        User us = extractCoefs(coefs);
        
        hm.put(MENU_MENU, prods);
        hm.put(MENU_SNACK, prodsSnack);
        hm.put(MENU_USER, us);
        hm.put(ERROR, null);
        
        return hm;
	}
	private User extractCoefs(String in){
        //ДБ 5 параметров
        String [] par = in.replace("<br>", "").split(" ");
        if (par.length!=5){
            return null;
        }
        
        User us = new User(user);
        us.getFactors().setK1Value( Float.parseFloat(par[0]) * 
                us.getFactors().getBEValue() / 10f );
        us.getFactors().setK2(Float.parseFloat(par[1]));
        us.getFactors().setK3(Float.parseFloat(par[2]));

        us.setS1(Float.parseFloat(par[3]));
        us.setS2(Float.parseFloat(par[4]));
        return us;
    }
	private String getFirstTagged(String output, String tag) throws Exception{
        String strs = "";
        String tagfullst = "<"+tag+">";
        String tagfullend = "</"+tag+">";
        int st = 0;
        int end = 0;
        if (output.length()>0){// && st>=0 && end>=0){
            st = output.indexOf(tagfullst);
            end = output.indexOf( tagfullend, st+tagfullst.length());
            if (st>=0 && end>=0){
                strs = output.substring(st+tagfullst.length(), end);
                output = output.substring(end+tagfullend.length());
            }
            else throw new Exception("no needed tag "+tag+" found");
        }
        else throw new Exception("empty string");
        return strs;
    }
	private ArrayList<ProductInMenu> parseMenu(String st,int type){
		ArrayList<ProductInMenu> arr = new ArrayList<ProductInMenu>();
        String [] lines = st.trim().split("<br>");
        for(int i=0;i<lines.length;i++){
            String [] param = lines[i].split(" ");
            int gi;
            float w,p,c,f;
            String name = "";
            if (param.length>6){ //0-меню 1-перекус
            	try{
            		int typeP = Integer.parseInt(param[param.length-1]);
            		if (type==typeP){
            			w = Float.parseFloat(param[param.length-2]);
            			gi = Integer.parseInt(param[param.length-3]);
            			c = Float.parseFloat(param[param.length-4]);
            			f = Float.parseFloat(param[param.length-5]);
            			p = Float.parseFloat(param[param.length-6]);
                    
            			for(int j=0;j<(param.length-6);j++){
            				name += param[j] + " ";
            			}
            			name = name.trim();
            		}else continue;//Если не тот тип, то идем к следующему
            	}catch(Exception ex){
                    gi = 0;
                    w = p = f = c = 0f;
                }
            	arr.add(new ProductInMenu(name,p,f,c,gi,w,-1));
            }
        }
        return arr;
    }
}
