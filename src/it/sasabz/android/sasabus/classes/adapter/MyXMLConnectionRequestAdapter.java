/**
 *
 * MyXMLConnectionRequestAdapter.java
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
package it.sasabz.android.sasabus.classes.adapter;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.hafas.XMLConnectionRequest;

import java.text.SimpleDateFormat;
import java.util.Vector;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class MyXMLConnectionRequestAdapter extends BaseAdapter {
	private final Vector<XMLConnectionRequest> list;

	
	/**
	 * This constructor creates an object with the following parameters
	 * @param context is the context to work with
	 * @param whereId is the resource id where to place the string
	 * @param layoutId is the layout id of the list_view
	 * @param list is the list of dbobject's which are to putting in the list_view
	 */
	public MyXMLConnectionRequestAdapter(Vector<XMLConnectionRequest> list) {
		this.list = list;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) 
		{
              LayoutInflater vi = (LayoutInflater)SASAbus.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
              v = vi.inflate(R.layout.connection_row_new, null);
		}
		if (list != null)
		{
			TextView departure = (TextView) v.findViewById(R.id.departure);
			TextView infos = (TextView) v.findViewById(R.id.info);
			TextView arrival = (TextView) v.findViewById(R.id.arrival);
			XMLConnectionRequest conreq = list.get(position);
			if(conreq != null)
			{
				SimpleDateFormat simple = new SimpleDateFormat("HH:mm");
				departure.setText(Html.fromHtml("<b>" + simple.format(conreq.getDeparture().getArrtime()) + " " + conreq.getDeparture().getStation().getHaltestelle() + "</b>"));
				infos.setText(simple.format(conreq.getDuration()) + " -> " + SASAbus.getContext().getResources().getString(R.string.transfers)  + ": "+ conreq.getTransfers());
				arrival.setText(Html.fromHtml("<b>" + simple.format(conreq.getArrival().getArrtime()) + " " + conreq.getArrival().getStation().getHaltestelle() + "</b>"));
			}
		}
		
		return v;
	}

	@Override
	public int getCount() {
		if(list == null)
			return 0;
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		if(list == null)
			return null;
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (list == null)
			return -1;
		return position;
	}

}
