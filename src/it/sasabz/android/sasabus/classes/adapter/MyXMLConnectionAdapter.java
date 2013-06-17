/**
 *
 * MyXMLConnectionAdapter.java
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
import it.sasabz.android.sasabus.classes.hafas.XMLConnection;
import it.sasabz.android.sasabus.classes.hafas.XMLJourney;
import it.sasabz.android.sasabus.classes.hafas.XMLWalk;

import java.text.SimpleDateFormat;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class MyXMLConnectionAdapter extends BaseAdapter {
	private final Vector<XMLConnection> list;

	
	/**
	 * This constructor creates an object with the following parameters
	 * @param context is the context to work with
	 * @param whereId is the resource id where to place the string
	 * @param layoutId is the layout id of the list_view
	 * @param list is the list of dbobject's which are to putting in the list_view
	 */
	public MyXMLConnectionAdapter(Vector<XMLConnection> list) {
		this.list = list;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (list != null)
		{
		    LayoutInflater vi = (LayoutInflater)SASAbus.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (position == 0) 
			{
				if (list.size() == 1) 
				{
					v = vi.inflate(R.layout.con_transfer_row_one, null);
				} 
				else 
				{
					v = vi.inflate(R.layout.con_transfer_row_first, null);
				}
			} 
			else 
			{
				if (position == list.size() - 1) 
				{
					v = vi.inflate(R.layout.con_transfer_row_last, null);
				}
				else 
				{
					v = vi.inflate(R.layout.con_transfer_row_follower, null);
				}
			}
			XMLConnection conreq = list.get(position);
			if(conreq != null)
			{
				SimpleDateFormat simple = new SimpleDateFormat("HH:mm");
				if (position == 0)
				{
					TextView departure = (TextView) v
							.findViewById(R.id.departure);
					departure.setText(Html.fromHtml("<b>"
							+ simple.format(conreq.getDeparture().getArrtime())
							+ " "
							+ conreq.getDeparture().getStation()
									.getHaltestelle() + "</b> "));
				}

				TextView arrival = (TextView) v.findViewById(R.id.arrival);
				arrival.setText(Html.fromHtml("<b>"
						+ simple.format(conreq.getArrival().getArrtime()) + " "
						+ conreq.getArrival().getStation().getHaltestelle()
						+ "</b> "));

				TextView info = (TextView) v.findViewById(R.id.info);
				ImageView image = (ImageView) v.findViewById(R.id.image);
				String infotext = "";
				if (conreq instanceof XMLWalk)
				{
					int random = (int)(Math.random() * 10) % 2;
					if(random == 1)
					{
						image.setImageResource(R.drawable.middle_rabbit);
					}
					else
					{
						image.setImageResource(R.drawable.middle_turtle);
					}
					infotext = simple.format(conreq.getDuration());
				}
				else if(conreq instanceof XMLJourney)
				{
					image.setImageResource(R.drawable.middle_bus);
					
					infotext = simple.format(conreq.getDuration());
					Resources res = SASAbus.getContext().getResources();
					infotext += (" -&gt; <font color=\""
							+ res.getColor(R.color.sasa_orange) + "\">"
							+ res.getString(R.string.line) + " "
							+ ((XMLJourney)conreq).getAttribut("NUMBER") + "</font>");
				}
				info.setText(Html.fromHtml(infotext));
				
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
