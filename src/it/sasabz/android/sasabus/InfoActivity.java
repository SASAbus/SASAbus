/**
 *
 * InfoActivity.java
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
 * This activity shows the informations retrieved from the webseite of sasabz.it.
 * 
 */
package it.sasabz.android.sasabus;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.classes.Information;
import it.sasabz.android.sasabus.classes.adapter.MyListAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.DBObject;
import it.sasabz.android.sasabus.classes.dialogs.About;
import it.sasabz.android.sasabus.classes.dialogs.Credits;
import it.sasabz.android.sasabus.classes.services.InformationList;

import java.util.ArrayList;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class InfoActivity extends ListActivity {

	//The List to store the retrieved informations (de/it) from the server
	private ArrayList<DBObject> list = null;
	
	//Is the progressbar to show during retrieving the information
	private ProgressDialog progdial = null;

	public InfoActivity() {
		// TODO Auto-generated constructor stub
	}

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.standard_listview_layout);
        TextView titel = (TextView)findViewById(R.id.untertitel);
		titel.setText(R.string.menu_infos);
	}

	
	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * If the phone has an internet connectivity, then the data is retrieved, 
		 * otherwise a dialog informs the user of the "powered off connection"
		 */
		if(haveNetworkConnection())
			fillData();
		else
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			builder.setMessage(R.string.no_network_connection);
			builder.setTitle(R.string.error_title);
			builder.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
	}

	/**
	 * This Method is reacting on an interaction from the user. If the user taps on a list element, so 
	 * a dialog is showing the entire message of this information.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(android.R.string.ok,
				new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
		Information information = (Information) list.get(position);
		builder.setTitle(Html.fromHtml(information.getTitel()));
		builder.setMessage(Html.fromHtml("<pre>" + information.getNachricht() + "</pre>"));
		builder.create().show();
	}

	/**
	 * this method calls the information-retriever which retrieves the information from the 
	 * server of SASA SpA-AG. The progress dialog willbe created and will be shown
	 */
	public void fillData() {
		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(this);
		int infocity = Integer.parseInt(shared.getString("infos", "0"));
		
		progdial = new ProgressDialog(this);
		
		progdial.setMessage(getResources().getText(R.string.waiting));
		progdial.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progdial.setCancelable(false);
		progdial.show();
		
		InformationList info = new InformationList(this);
		info.execute(Integer.valueOf(infocity));
	}

	/**
	 * this is the method which fills the information containing list
	 * @param list is an array list of informations
	 */
	public void fillList(ArrayList<DBObject> list)
	{
		this.list = list;
		MyListAdapter infos = new MyListAdapter(SASAbus.getContext(),
				R.id.text, R.layout.news_row, list);
		setListAdapter(infos);
		progdial.dismiss();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about: {
			new About(this).show();
			return true;
		}
		case R.id.menu_credits: {
			new Credits(this).show();
			return true;
		}
		}
		return false;
	}
	
	
	 /**
	 * this method checks if a networkconnection is active or not
	 * @return boolean if the network is reachable or not
	 */
	private boolean haveNetworkConnection() 
	{
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) (this.getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			//testing WIFI connection
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			//testing GPRS/EDGE/UMTS/HDSPA/HUSPA/LTE connection
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}