package org.diacalc.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.diacalc.android.components.FloatEditText;
import org.diacalc.android.internet.DoingPost;
import org.diacalc.android.manager.DatabaseManager;
import org.diacalc.android.maths.User;
import org.diacalc.android.products.ProductGroup;
import org.diacalc.android.products.ProductInBase;
import org.diacalc.android.products.ProductInMenu;
import org.diacalc.android.products.ProductFeatures;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.AlertDialog;
import android.app.Dialog;

public class ProdForm extends ListActivity{
	
	private TextView groupTitle; 
	
	private int groupSelected = -1;
	private int prodSelected = -1;
	
	private ProductProxyAdapter adapter;
	
	private ArrayList<ProductGroup> groups;
	private ArrayList<ProductInBase> prods;
	private DatabaseManager mgr;
	private User user;
    private DataPocket dtPkt;
	
	//для использования в отдельном потоке
    private ArrayList<ProductInBase> i_prods = null;
    private ArrayList<ProductGroup> i_group = null;
    private String i_msg = "";
    private ProgressDialog i_progressDialog = null;
    
    private static final int DIALOG_PROD_NEW_ID = 200+0;
    private static final int DIALOG_PROD_EDIT_ID = 200+1;
    private static final int DIALOG_GROUP_ID = 200+2;
    
    private boolean searchMode = false;
    private ArrayList<ProductInMenu> menuProds;
    
    private Comparator<ProductW> prodsComparator = new Comparator<ProductW>(){
		@Override
		public int compare(ProductW p1, ProductW p2) {
			return p1.getName().compareTo(p2.getName());
		}
    	
    };
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prods);
		
		
		
		mgr = new DatabaseManager(this);
		
		dtPkt = (DataPocket)this.getApplication();
		user = dtPkt.getUser(mgr);
		groupTitle = (TextView)findViewById(R.id.textProdGroupName);
		
		groups = dtPkt.getGroups(mgr);
		prods = dtPkt.getProducts(mgr);
		menuProds = dtPkt.getMenuProds(mgr);
		
		
		adapter = new ProductProxyAdapter(this, 
				dtPkt.getProducts(mgr));
		
		if (savedInstanceState!=null){
			groupSelected = savedInstanceState.getInt("selectedGroup");
			prodSelected = savedInstanceState.getInt("selectedRow");
			
			adapter.filter(groups.get(groupSelected).getId());
		}else if (!groups.isEmpty()){ 
        	groupSelected = 0;
    		adapter.filter(groups.get(groupSelected).getId());
    	}
		
		setGroupName(groupSelected);
		
		getListView().setAdapter(adapter);
		
		if (prodSelected>-1) getListView().setSelection(prodSelected);
		
		//getListView().setTextFilterEnabled(true);
		
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, 
					View view, 
					int position, 
					long id){
				ProductInBase pr = (ProductInBase)adapter.getItem(position);
				if (pr.isSelected()){//Убираем его из меню 
					for(int i=0;i<menuProds.size();i++){
						if (menuProds.get(i).getId()==pr.getId()){
							menuProds.remove(i);
							break;
						}
					}
				}else{
					menuProds.add(new ProductInMenu(pr));
					//тут нужна сортировка
					Collections.sort(menuProds, prodsComparator);
				}
				dtPkt.setMenuNeed2Save();
				
				pr.setSelected(!pr.isSelected());
				adapter.notifyDataSetChanged();
			}
			
		});
		registerForContextMenu(getListView());
		
		// Get the intent, verify the action and get the query
	    /*Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      Log.i("searching",query);
	      //doMySearch(query);
	    }*/
	}
	public void onStarButtonClick(View v){
		Intent intent = new Intent();
        intent.setClass(getBaseContext(), MenuForm.class);
        startActivity(intent);
        finish();
	}
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
		
	  savedInstanceState.putInt("selectedGroup", groupSelected);
	  savedInstanceState.putInt("selectedRow", getListView().getSelectedItemPosition());
	  
	  super.onSaveInstanceState(savedInstanceState);
	}
	private void setGroupName(int sel){
		if (groups.isEmpty()){
			 groupSelected = -1;
			 groupTitle.setText(getString(R.string.noGroups));
		}
		else{
			groupSelected = sel;
			groupTitle.setText(""+(groupSelected+1)+". "+
					groups.get(groupSelected).getName());
		}
	}
	public void onClickMoveLeft(View v){
		if (searchMode || groups.isEmpty()) return;
            if (groupSelected>0)	groupSelected--;
            else					groupSelected = groups.size()-1;
            setGroupName(groupSelected);
            adapter.filter(groups.get(groupSelected).getId());
    }
	public void onClickMoveRight(View v){
		if (searchMode || groups.isEmpty()) return;
            if (groupSelected<(groups.size()-1) ) 
            		groupSelected++;
            else 	groupSelected = 0;
            setGroupName(groupSelected);
            adapter.filter(groups.get(groupSelected).getId());
	}
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.prods, menu);
        return true;
    }
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработка меню
        switch (item.getItemId()) {
        case R.id.downloadProducts:
            downloadProducts();
            return true;
        case R.id.createProductSub:
        	showDialog(DIALOG_PROD_NEW_ID);
        	return true;
        /*case R.id.deleteProductSub:
        	deleteProduct();
        	return true;*/
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                                ContextMenuInfo menuInfo) {
	  super.onCreateContextMenu(menu, v, menuInfo);
	  MenuInflater inflater = getMenuInflater();
	  if (v==getListView()){
		  inflater.inflate(R.menu.prods_context, menu);
		  AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		  menu.setHeaderTitle(
				  ((ProductInBase)adapter.getItem(info.position)).getName()
				  );
	  }
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.deleteProductProd:
	    deleteProduct(info.position);
	    return true;
	  case R.id.createNewProductProd:
		showDialog(DIALOG_PROD_NEW_ID);
		return true;
	  case R.id.changeProductProd:
		showDialog(DIALOG_PROD_EDIT_ID,info.position);
		return true;
	  default:
	    return super.onContextItemSelected(item);
	  }
	}
	private int productWasSelected = -1;
	private void showDialog(int id,int pos){
		productWasSelected = pos;
		showDialog(id);
		productWasSelected = -1;
	}
	private void deleteProduct(int pos){
		final ProductInBase prod = (ProductInBase)adapter.getItem(pos);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.Delete))
			.setMessage(getString(R.string.deleteProduct)+":\n"+prod.getName())
		.setPositiveButton(getString(R.string.btnOk),new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				prods.remove(prod);
				mgr.deleteProduct(prod);
				adapter.filter(groups.get(groupSelected).getId());
			}
        })
        .setNegativeButton(getString(R.string.btnNo),new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
        });
		AlertDialog alert = builder.create();
    	alert.show();
	}
	private void downloadProducts(){
		Thread t = new Thread(){
			public void run(){
				i_prods = null;
        		i_group = null;
        		i_msg = "";
        		
				HashMap<String,Object> i_answer = 
        			new DoingPost(user).requestProducts();
    	    	if (i_answer.get(DoingPost.ERROR)!=null){
    	    		//Значит ошибка
    	    		i_msg = (String)i_answer.get(DoingPost.ERROR);
    	    	}else{
    	    		i_prods = (ArrayList<ProductInBase>)i_answer
    	    				.get(DoingPost.PRODS_PRODS);
    	    		i_group = (ArrayList<ProductGroup>)i_answer
    	    				.get(DoingPost.PRODS_GROUP);
    	    		//И вот тут надо сохранить в БД, т.к. это займет время
    	    		mgr.putProducts(i_group, i_prods);
    	    		dtPkt.setGroupProds2Null();
    	    		groups = dtPkt.getGroups(mgr);
    	    		prods = dtPkt.getProducts(mgr);
    	    		dtPkt.setProdsNeed2Save();
    	    	}
    	    	
				
				runOnUiThread(new Runnable(){
					public void run(){
						i_progressDialog.dismiss();
						
						AlertDialog.Builder builder = 
		    	    		new AlertDialog.Builder(ProdForm.this);
		    	    	if (i_msg.length()>0){//Выводим сообщение о ошибке
		    	    		builder.setTitle(getString(R.string.errorTitle))
		    	    			.setMessage(getString(R.string.errorMsgProdsLoad)+"\n"+i_msg)
		    	    			.setNeutralButton(getString(R.string.btnOk),new DialogInterface.OnClickListener(){
		    	        			public void onClick(DialogInterface dialog, int id) {
		    	    					dialog.cancel();
		    	        			}
		    	    	        });
		    	    		
		    	        }else{
		    	        	builder.setTitle(getString(R.string.prodsLoadedTitle))
		    	        	.setMessage(getString(R.string.prodsLoadedMsg))
	    	    			.setNeutralButton(getString(R.string.btnOk),new DialogInterface.OnClickListener(){
	    	        			public void onClick(DialogInterface dialog, int id) {
	    	    					//тут заполняем продуктами
	    	        				int owner = -1;
	    	        				setGroupName(0);
	    	        				if (groupSelected>-1){
	    	        					owner = groups.get(groupSelected).getId();
	    	        				}
	    	        				adapter.changeProds(prods, owner);
	    	        			}
	    	    	        });
		    	        }
		    	    	AlertDialog alert = builder.create();
    	    	    	alert.show();
					}
				});
			}
		};
		
		i_progressDialog = ProgressDialog.show(this,    
                getString(R.string.pleaseWait), 
                getString(R.string.prodsLoading), true);
		t.start();
	}
	
	/* Работа с диалогами
     */
    
    @Override
    public Dialog onCreateDialog(int id) {
    	switch(id){
    	case DIALOG_PROD_NEW_ID:
    	case DIALOG_PROD_EDIT_ID:
    			return createProdDlg();
    	/*case DIALOG_GROUP_ID:
    			return createGroupDlg();*/
    	default: return null;	
    	}
    	
    }
    //Подготавливаем диалоги
    @Override
	public void onPrepareDialog(int id, Dialog dialog){
		switch (id){
			case DIALOG_PROD_NEW_ID:
				prepareProductDialog(dialog, null);
				break;
			case DIALOG_PROD_EDIT_ID:
				prepareProductDialog(dialog, (ProductInBase)adapter.getItem(productWasSelected));
				break;
		}
    }
    private void prepareProductDialog(final Dialog dialog,final ProductInBase prod){
    	final Spinner sp = (Spinner)dialog.findViewById(R.id.spinnerGroupSelectProdDlg);
		
		ArrayAdapter<ProductGroup> adapter = new ArrayAdapter<ProductGroup>(
				ProdForm.this,
				android.R.layout.simple_spinner_item,
				groups); 
			
		sp.setAdapter(adapter);
		if (prod==null){
			dialog.setTitle(getString(R.string.newProduct));
			((EditText)dialog.findViewById(R.id.editNameProdDlg))
				.setText("");
			((FloatEditText)dialog.findViewById(R.id.editWeightProdDlg))
				.setValue(100f);
			((FloatEditText)dialog.findViewById(R.id.editProtProdDlg))
				.setValue(0f);
			((FloatEditText)dialog.findViewById(R.id.editFatProdDlg))
				.setValue(0f);
			((FloatEditText)dialog.findViewById(R.id.editCarbProdDlg))
				.setValue(0f);
			((FloatEditText)dialog.findViewById(R.id.editGiProdDlg))
				.setValue(50f);
			sp.setSelection(groupSelected);
		}else{
			dialog.setTitle(getString(R.string.editProduct));
			((EditText)dialog.findViewById(R.id.editNameProdDlg))
				.setText(prod.getName());
			((FloatEditText)dialog.findViewById(R.id.editWeightProdDlg))
				.setValue(prod.getWeight());
			((FloatEditText)dialog.findViewById(R.id.editProtProdDlg))
				.setValue(prod.getAllProt());
			((FloatEditText)dialog.findViewById(R.id.editFatProdDlg))
				.setValue(prod.getAllFat());
			((FloatEditText)dialog.findViewById(R.id.editCarbProdDlg))
				.setValue(prod.getAllCarb());
			((FloatEditText)dialog.findViewById(R.id.editGiProdDlg))
				.setValue(prod.getGi());
			int p = 0;
			for(int i=0;i<groups.size();i++){
				if (prod.getOwner()==groups.get(i).getId()){
					p = i;
					break;
				}
			}
			sp.setSelection(p);
		}
		Button btnOk = (Button)dialog.findViewById(R.id.btnProdDlgOk);
		btnOk.setOnClickListener(null);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// А тут делаем некие действия по созданию продукта
				groupSelected = sp.getSelectedItemPosition();
				int grId = groups.get(groupSelected).getId();
				
				if (prod==null){//Создаем новый
					float w = ((FloatEditText)dialog.findViewById(R.id.editWeightProdDlg))
					.getValue();
					ProductInBase pr = new ProductInBase(
						((EditText)dialog.findViewById(R.id.editNameProdDlg))
						.getText().toString(),
						100f * ((FloatEditText)dialog.findViewById(R.id.editProtProdDlg))
						.getValue() / w,
						100f* ((FloatEditText)dialog.findViewById(R.id.editFatProdDlg))
						.getValue() / w,
						100f* ((FloatEditText)dialog.findViewById(R.id.editCarbProdDlg))
						.getValue() / w,
						(int)((FloatEditText)dialog.findViewById(R.id.editGiProdDlg))
						.getValue(),
						w,
						true,//mobile
						grId,//Владелец
						0,
						-1
					);
					
					pr.setWeight(100f);
					
					mgr.insertProduct(pr);
					prods.add(pr);
					
				}else{
					float w = ((FloatEditText)dialog.findViewById(R.id.editWeightProdDlg))
					.getValue();
					prod.setName(((EditText)dialog.findViewById(R.id.editNameProdDlg))
							.getText().toString());
					prod.setProt(100f * ((FloatEditText)dialog.findViewById(R.id.editProtProdDlg))
							.getValue() / w);
					prod.setFat(100f * ((FloatEditText)dialog.findViewById(R.id.editFatProdDlg))
						.getValue() / w);
					prod.setCarb(100f * ((FloatEditText)dialog.findViewById(R.id.editCarbProdDlg))
						.getValue()/ w);
					prod.setGi((int)((FloatEditText)dialog.findViewById(R.id.editGiProdDlg))
							.getValue());
					prod.setWeight(w);
					prod.setMobile(true);
					prod.setOwner(grId);
					
					prod.setWeight(100f);
					
					mgr.changeProduct(prod);
				}
				dialog.dismiss();
				
				Collections.sort(prods, prodsComparator);
				
				setGroupName(groupSelected);
				
				ProdForm.this.adapter.changeProds(prods, groups.get(groupSelected).getId());
			}
		});
		Button btnNo = (Button)dialog.findViewById(R.id.btnProdDlgNo);
		btnNo.setOnClickListener(null);
    	btnNo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Тут просто закрываем
				dialog.dismiss();
			}
		});
    }
    private Dialog createProdDlg(){
    	final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.product_dialog);
    	((FloatEditText)dialog.findViewById(R.id.editGiProdDlg))
    		.setZeroes(0);
    	EditText name = (EditText)dialog.findViewById(R.id.editNameProdDlg);
    	FloatEditText weight = (FloatEditText)dialog.findViewById(R.id.editWeightProdDlg);
    	FloatEditText prot = (FloatEditText)dialog.findViewById(R.id.editProtProdDlg);
    	FloatEditText fat = (FloatEditText)dialog.findViewById(R.id.editFatProdDlg);
    	FloatEditText carb = (FloatEditText)dialog.findViewById(R.id.editCarbProdDlg);
    	FloatEditText gi = (FloatEditText)dialog.findViewById(R.id.editGiProdDlg);
    	
    	name.setNextFocusDownId(R.id.editWeightProdDlg);
    	weight.setNextFocusDownId(R.id.editProtProdDlg);
    	prot.setNextFocusDownId(R.id.editFatProdDlg);
    	fat.setNextFocusDownId(R.id.editCarbProdDlg);
    	carb.setNextFocusDownId(R.id.editGiProdDlg);
    	gi.setNextFocusDownId(R.id.spinnerGroupSelectProdDlg);
    	
    	return dialog;
    }
	private class ProductProxyAdapter extends BaseAdapter{
		private ArrayList<Integer> filter = null;
		private ArrayList<ProductInBase> underlying;//неотфильтрованные продукты
		private LayoutInflater mInflater;
		
		public ProductProxyAdapter(Context c,ArrayList<ProductInBase> prods){
			// Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(c);
            underlying = prods;
        }
		public void changeProds(ArrayList<ProductInBase> prods,int owner){
			underlying = prods;
			filter(owner);
		}
		private int getFilterOffset(int index) {
		    if(filter == null) {
		         return index;
		    }
		    if(filter.size() > index && index>=0) {
		    	return filter.get(index).intValue();
		    }
		    return -1;
		}
		private int getUnderlyingOffset(int index) {
		    if(filter == null) {
		        return index;
		    }
		    return filter.indexOf(new Integer(index));
		}
		public void filter(int owner) {
		    filter = new ArrayList<Integer>();
		    for(int iter = 0 ; iter < underlying.size() ; iter++) {
		           ProductInBase element = (ProductInBase)underlying.get(iter);
		           if( (element.getOwner()==owner 
		        		   || owner==-1) ) {
		               filter.add(new Integer(iter));
		           }
		    }
		    notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			/*if (underlying!=null) return underlying.size();
			else return 0;*/
			if (filter!=null) return filter.size();
			else return 0;
		}

		@Override
		public Object getItem(int index) {
			if (index<0) return null;
		    return underlying.get(getFilterOffset(index));
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.product_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.textProductName);
                holder.descr = (TextView) convertView.findViewById(R.id.textProductDescription);
                
                holder.selected = (CheckBox)convertView.findViewById(R.id.checkProductSelected);
                
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.name.setText( ((ProductInBase)getItem(position)).getName() );
            holder.descr.setText( ""+
            		underlying.get(getFilterOffset(position)).getProt()+"-"+
            		underlying.get(getFilterOffset(position)).getFat()+"-"+
            		underlying.get(getFilterOffset(position)).getCarb()+"-"+
            		underlying.get(getFilterOffset(position)).getGi() );
            holder.selected.setChecked(underlying.get(getFilterOffset(position)).isSelected());
            
            return convertView;
        }
		class ViewHolder{
			TextView name;
			TextView descr;
			CheckBox selected;
		}
		
	}
}
