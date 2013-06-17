/**
 *
 * ConnectionDialog.java
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

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.classes.adapter.MyXMLConnectionAdapter;
import it.sasabz.android.sasabus.classes.hafas.XMLConnection;
import it.sasabz.android.sasabus.classes.hafas.XMLJourney;
import it.sasabz.android.sasabus.fragments.WayFragment;

import java.text.SimpleDateFormat;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class ConnectionDialog extends Dialog{

	private Vector<XMLConnection> list = null;
	
	private Fragment fragment = null;
	
	public ConnectionDialog(Fragment fragment, Vector<XMLConnection> list) {
		super(fragment.getActivity());
		this.list = list;
		this.fragment = fragment;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setContentView(R.layout.transfer_listview_layout);
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		setTitle(R.string.connection_details);
		fillData();
	}
	
	public Context getThis()
	{
		return this.getContext();
	}
	
	private void fillData()
	{
		MyXMLConnectionAdapter adapter = new MyXMLConnectionAdapter(list);
		ListView listv = (ListView)findViewById(android.R.id.list);
		listv.setAdapter(adapter);
		listv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
				XMLConnection conn = list.get(position);
				if(conn instanceof XMLJourney)
				{
					SimpleDateFormat simple = new SimpleDateFormat("HH:mm");
					String fromtext = "(" + conn.getDeparture().getStation().getHaltestelle().replace(" -", ")");
					String totext = "(" + conn.getArrival().getStation().getHaltestelle().replace(" -", ")"); 
					FragmentManager fragmentManager = fragment.getFragmentManager();
					FragmentTransaction ft = fragmentManager.beginTransaction();
					
					Fragment old = fragmentManager.findFragmentById(R.id.onlinefragment);
					if(old != null)
					{
						ft.remove(old);
					}
					Fragment fragment = null;
					try
					{
						fragment = new WayFragment(((XMLJourney)conn).getAttribut("NUMBER"), fromtext, totext, 
								simple.format(conn.getDeparture().getArrtime()), 
								simple.format(conn.getArrival().getArrtime()));
					}
					catch(Exception e)
					{
						fragment = old;
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setCancelable(false);
						builder.setMessage(R.string.error_connection);
						builder.setTitle(R.string.error);
						builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

									dialog.dismiss();
							}
						});
						builder.create();
						builder.show();
						fragmentManager.popBackStack();
					}
					ft.add(R.id.onlinefragment, fragment);
					ft.addToBackStack(null);
					ft.commit();
					fragmentManager.executePendingTransactions();
					dismiss();
				}
			}
		});
	}
	
	

	

}
