/**
 *
 * SelectLineaActivity.java
 * 
 * Created: Jan 16, 2011 11:41:06 AM
 * 
 * Copyright (C) 2011 Paolo Dongilli and Markus Windegger
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

package it.sasabz.android.sasabus.fragments;

import java.util.Vector;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.adapter.MyListAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.Bacino;
import it.sasabz.android.sasabus.classes.dbobjects.BacinoList;
import it.sasabz.android.sasabus.classes.dbobjects.DBObject;
import it.sasabz.android.sasabus.classes.dbobjects.Linea;
import it.sasabz.android.sasabus.classes.dbobjects.LineaList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class LineaFragment extends Fragment implements OnItemClickListener {
	
	//this vector provides the list of lines in the entire activity
    private Vector<DBObject> list = null;
    
    private Bacino bacino = null;
    
    private LineaFragment() {
    }

    public LineaFragment(int bacino)
    {
    	this();
    	this.bacino = BacinoList.getById(bacino);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	View result = inflater.inflate(R.layout.standard_listview_layout, container, false);
    	
    	 TextView titel = (TextView)result.findViewById(R.id.untertitel);
         titel.setText(R.string.select_linea);
    	
    	fillData(result);
    	return result;
    }
    
    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


  
    
    /**
     * this method fills the list_view with the lines which are situated into the bacino bacino
     * @param bacino is the bacino chosen for getting the lines
     */
    private void fillData(View result) {
    	list = LineaList.getList(bacino.getTable_prefix());
    	list = LineaList.sort(list);
    	MyListAdapter linee = new MyListAdapter(SASAbus.getContext(), R.id.text, R.layout.linea_row, list);
    	ListView listview = (ListView)result.findViewById(android.R.id.list);
    	listview.setAdapter(linee);
        listview.setOnItemClickListener(this);
    }  
    

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		// TODO Auto-generated method stub
        Linea linea = (Linea)list.get(position); 
        FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		
		Fragment fragment = fragmentManager.findFragmentById(R.id.onlinefragment);
		if(fragment != null)
		{
			ft.remove(fragment);
		}
		fragment = new DepartureFragment(bacino, linea);
		ft.add(R.id.onlinefragment, fragment);
		ft.addToBackStack(null);
		ft.commit();
		fragmentManager.executePendingTransactions();
	}
}
