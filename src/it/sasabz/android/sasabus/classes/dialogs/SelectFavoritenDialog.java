/**
 *
 * SelectFavoritenActivity.java
 * 
 * 
 * Copyright (C) 2012 Markus Windegger
 *
 * This file is part of SasaBus.

 * SasaBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SasaBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package it.sasabz.android.sasabus.classes.dialogs;

import java.util.Locale;
import java.util.Vector;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.Favorit;
import it.sasabz.android.sasabus.classes.FavoritenDB;
import it.sasabz.android.sasabus.classes.FavoritenList;
import it.sasabz.android.sasabus.classes.adapter.MyFavoritenListAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.fragments.OnlineSearchFragment;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

public class SelectFavoritenDialog extends Dialog implements OnItemClickListener {

    
    private Vector<Favorit> list = null;
    
    private OnlineSearchFragment fragment = null;
    
    public SelectFavoritenDialog(OnlineSearchFragment fragment) {
    	super(fragment.getActivity());
    	this.fragment = fragment;
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.favoriten_listview);
        
        this.setTitle(R.string.mode_favoriten);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    	lp.copyFrom(getWindow().getAttributes());
    	lp.width = LayoutParams.FILL_PARENT;
    	lp.height = LayoutParams.FILL_PARENT;
    	getWindow().setAttributes(lp);
    	Display dm = getWindow().getWindowManager().getDefaultDisplay();
        int width = dm.getWidth();
        int height = dm.getHeight();
        getWindow().setLayout(width/5*4, height/2);
        fillData();
    }

    
    @Override
    public void onItemClick(AdapterView<?> av, View l, int position, long id) {
        Favorit fav = list.get(position);
        String namefrom = fav.getPartenza();
        String nameto = fav.getDestinazione();
        
        String namefromloc = "";
        String nametoloc = "";
        
        if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			String italienischfrom = namefrom.substring(0, namefrom.indexOf("-")).trim();
			Palina stationfrom = PalinaList.getTranslation(italienischfrom, "it");
			if(stationfrom != null)
			{
				namefromloc = stationfrom.getName_de();
				namefromloc =  namefromloc.substring(1, namefromloc.indexOf(")"))+ " - " +
						namefromloc.substring(namefromloc.indexOf(")") + 1).trim();
			}
			else
			{
				String geteilt = namefrom.substring(namefrom.indexOf("-") + 1).trim();
				namefromloc =  geteilt.substring(1, geteilt.indexOf(")"))+ " - " +
						geteilt.substring(geteilt.indexOf(")") + 1).trim();
			}
			String italienischto = nameto.substring(0, nameto.indexOf("-")).trim();
			Palina stationto = PalinaList.getTranslation(italienischto, "it");
			if(stationto != null)
			{
				nametoloc = stationto.getName_de();
				nametoloc =  nametoloc.substring(1, nametoloc.indexOf(")"))+ " - " +
						nametoloc.substring(nametoloc.indexOf(")") + 1).trim();
			}
			else
			{
				String geteilt = nameto.substring(nameto.indexOf("-") + 1).trim();
				nametoloc =  geteilt.substring(1, geteilt.indexOf(")"))+ " - " +
						geteilt.substring(geteilt.indexOf(")") + 1).trim();
			}
		}
		else
		{
			String geteiltfrom = namefrom.substring(0, namefrom.indexOf("-")).trim();
			namefromloc = geteiltfrom.substring(1, geteiltfrom.indexOf(")")) + " - " + 
					geteiltfrom.substring(geteiltfrom.indexOf(")") + 1).trim();
			String geteiltto = nameto.substring(0, namefrom.indexOf("-")).trim();
			nametoloc = geteiltto.substring(1, geteiltto.indexOf(")")) + " - " + 
					geteiltto.substring(geteiltto.indexOf(")") + 1).trim();
		}
        
        View result = fragment.getResult();
        AutoCompleteTextView from = (AutoCompleteTextView)result.findViewById(R.id.from_text);
        AutoCompleteTextView to = (AutoCompleteTextView)result.findViewById(R.id.to_text);
        
        from.setText(namefromloc);
        to.setText(nametoloc);
        
        
    	this.dismiss();
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
      if (v.getId() == android.R.id.list) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(list.get(info.position).toString());
        menu.add(Menu.NONE, 0, 0, R.string.delete);
      }
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if(menuItemIndex == 0)
        {
      	  Favorit fav = list.get(info.position);
      	  SQLiteDatabase db = new FavoritenDB(getContext()).getWritableDatabase();
      	  if(fav.delete(db))
      		  Log.v("FAVORITENLOESCHEN", "OK");
      	  else 
      		  Log.v("FAVORITENLOESCHEN", "FEHLER");
      	  db.close();
      	  list.remove(info.position);
      	  MyFavoritenListAdapter favoriten = new MyFavoritenListAdapter(SASAbus.getContext(), list);
      	  ListView liste = (ListView)findViewById(android.R.id.list);
      	  liste.setAdapter(favoriten);
        }
        return false;
    }
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      int menuItemIndex = item.getItemId();
      if(menuItemIndex == 0)
      {
    	  Favorit fav = list.get(info.position);
    	  SQLiteDatabase db = new FavoritenDB(getContext()).getWritableDatabase();
    	  if(fav.delete(db))
    		  Log.v("FAVORITENLOESCHEN", "OK");
    	  else 
    		  Log.v("FAVORITENLOESCHEN", "FEHLER");
    	  db.close();
    	  list.remove(info.position);
    	  MyFavoritenListAdapter favoriten = new MyFavoritenListAdapter(SASAbus.getContext(), list);
    	  ListView liste = (ListView)findViewById(android.R.id.list);
    	  liste.setAdapter(favoriten);
      }
      return false;
    }
    
    
    /**
     * fills the list_view with the modes which are offered to the user
     */
    public void fillData()
    {
    	 list = FavoritenList.getList();
    	 MyFavoritenListAdapter favoriten = new MyFavoritenListAdapter(SASAbus.getContext(), list);
    	 ListView liste = (ListView)findViewById(android.R.id.list);
         liste.setAdapter(favoriten); 
         registerForContextMenu(liste);
         liste.setOnItemClickListener(this);
         liste.setOnCreateContextMenuListener(this);
    }
    


	
}
