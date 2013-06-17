/**
 *
 * OnlineSelectStopActivity.java
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

package it.sasabz.android.sasabus.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.classes.adapter.MyXMLStationListAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.classes.hafas.XMLRequest;
import it.sasabz.android.sasabus.classes.hafas.XMLStation;
import it.sasabz.android.sasabus.classes.hafas.services.XMLStationList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OnlineSelectFragment extends Fragment{

    private ProgressDialog progress = null;
    private String from = "";
    private String to = "";
    private String date = "";
    private Palina fromPalina = null;
    private Palina toPalina = null;
    private Date datum = null;
    private Spinner from_spinner = null;
    private Spinner to_spinner = null;
    private Button search;
    private View result = null;
    
    
    public static final int XML_FAILURE = 0;
    public static final int NO_DATA = 1;
	
	private XMLStationList statlist = null;
    
    private OnlineSelectFragment()
    {
    }
    
    public OnlineSelectFragment(String from, String to, String date) {
    	this();
    	this.from = from;
    	this.to = to;
    	this.date = date;
    }

    private Context getContext()
    {
    	return this.getActivity();
    }
    
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	if(!XMLRequest.haveNetworkConnection())
        {
        	Toast.makeText(getContext(), R.string.no_network_connection, Toast.LENGTH_LONG).show();
        	getFragmentManager().popBackStack();
        	return null;
        }
        

		String lang = "it";
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
			lang = "de";
		fromPalina = PalinaList.getTranslation(from, lang);
		toPalina = PalinaList.getTranslation(to, lang);
		SimpleDateFormat simple = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		boolean plus12 = false;
		if(date.contains("PM"))
		{
			plus12 = true;
		}
		try
		{
			datum = simple.parse(date);
			if(plus12)
				datum.setHours(datum.getHours() + 12);
		}
		catch(Exception e)
		{
			Log.v("Datumsfehler", "Das Datum hat eine falsche Formatierung angenommen!!!");
			Toast.makeText(getContext(), "ERROR", Toast.LENGTH_LONG).show();
			getFragmentManager().popBackStack();
			return null;
		}
		
		 if(from == "" || to == "")
	        {
	        	Toast.makeText(getContext(), "ERROR", Toast.LENGTH_LONG).show();
	        	Log.v("SELECT STOP ERROR", "From: " + from + " | To: " + to);
	        	getFragmentManager().popBackStack();
	        	return null;
	        }
		
        progress = new ProgressDialog(getContext());
        progress.setMessage(getResources().getText(R.string.waiting));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();
        
        statlist = new XMLStationList(this);
        if(toPalina != null)
        {
        	to = toPalina.getHaltestelle();
        	Log.v("ONLINE-SELECT-TO", to);
        }
        if(fromPalina != null)
        {
        	from = fromPalina.getHaltestelle();
        	Log.v("ONLINE-SELECT-FROM", from);
        }
        statlist.execute(from, to);
        result = inflater.inflate(R.layout.online_select_layout, container, false);
        result.setVisibility(View.INVISIBLE);
        return result;
    }
    
    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    
    public void fillSpinner(Vector<XMLStation> from_list, Vector<XMLStation> to_list)
    {
    	String datetimestring = "";
    	SimpleDateFormat simple = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        datetimestring = simple.format(datum);
        
    	 if(from_list.size() == 1 && to_list.size() == 1)
         {
    		Log.v("Check", "Check");
         	XMLStation from = from_list.firstElement();
 			XMLStation to = to_list.firstElement();
 			
 			getConnectionList(from, to, datetimestring);
         }
    	 else if(from_list.size() == 1 && to.contains(to_list.get(0).getName()))
    	 {
    		Log.v("Check", "Check");
          	XMLStation from = from_list.firstElement();
  			XMLStation to = to_list.firstElement();
  			
  			getConnectionList(from, to, datetimestring);
    	 }
    	 else if(to_list.size() == 1 && from.contains(from_list.get(0).getName()))
    	 {
    		Log.v("Check", "Check");
          	XMLStation from = from_list.firstElement();
  			XMLStation to = to_list.firstElement();
  			
  			getConnectionList(from, to, datetimestring);
    	 }
    	 else
    	 {
	    	
	    	
	    	
	    	TextView datetime = (TextView)result.findViewById(R.id.time);
	        
	        datetime.setText(datetimestring);
	        
	        progress.dismiss();
	        
	        
	        if(from_list == null || to_list == null)
	        {
	        	Toast.makeText(getContext(), R.string.online_connection_error, Toast.LENGTH_LONG).show();
	        	getFragmentManager().popBackStack();
	        	return;
	        }
	        
	        
	        from_spinner = (Spinner) result.findViewById(R.id.from_spinner);
	        to_spinner = (Spinner) result.findViewById(R.id.to_spinner);
	        
	       
	        
	        // Create an ArrayAdapter using the string array and a default spinner layout
	        MyXMLStationListAdapter from_adapter = new MyXMLStationListAdapter(getContext(), from_list);
	        // Create an ArrayAdapter using the string array and a default spinner layout
	        MyXMLStationListAdapter to_adapter = new MyXMLStationListAdapter(getContext(), to_list);
	        
	        
	        // Apply the adapter to the spinner
	        from_spinner.setAdapter(from_adapter);
	        // Apply the adapter to the spinner
	        to_spinner.setAdapter(to_adapter);
	        
	        search = (Button)result.findViewById(R.id.search);
	        
	        search.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					XMLStation from = (XMLStation)from_spinner.getSelectedItem();
					XMLStation to = (XMLStation)to_spinner.getSelectedItem();
					TextView datetime = (TextView)result.findViewById(R.id.time);
					getConnectionList(from, to, datetime.getText().toString());
				}
			});
	        result.setVisibility(View.VISIBLE);
    	 }
    }
    
    public void getConnectionList(XMLStation from, XMLStation to, String datetime)
    {
    	progress.dismiss();
    	//Intent showConnection = new Intent(getContext(), OnlineShowFragment.class);
    	Fragment fragment = new OnlineShowFragment(from, to, datetime);
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		
		Fragment old = fragmentManager.findFragmentById(R.id.onlinefragment);
		if(old != null)
		{
			ft.remove(old);
		}
		ft.add(R.id.onlinefragment, fragment);
		ft.disallowAddToBackStack();
		ft.commit();
		fragmentManager.executePendingTransactions();
    }
    
    
    public AlertDialog getErrorDialog(String message)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setCancelable(false);
		builder.setMessage(message);
		builder.setTitle(R.string.error);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				getFragmentManager().popBackStack();
			}
		});
		return builder.create();
    }
    
    public void myShowDialog(int res)
    {
    	if(progress != null)
    		progress.dismiss();
    	switch(res)
    	{
    		case XML_FAILURE:
    			getErrorDialog(getResources().getString(R.string.error_station)).show();
    		break;
    		case NO_DATA:
    			getErrorDialog(getResources().getString(R.string.error_connection)).show();
    		break;
    	}
    }
    
}
