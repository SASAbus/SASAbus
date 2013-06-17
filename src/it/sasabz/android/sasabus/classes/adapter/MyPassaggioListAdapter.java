/**
 *
 * MyPassaggioListAdapter.java
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
import it.sasabz.android.sasabus.classes.dbobjects.Passaggio;

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class MyPassaggioListAdapter extends BaseAdapter {
	private final Context context;
	private final Vector<Passaggio> list;
	private final int layoutId;
	private final int whereId;
	private final int actpos;

	
	/**
	 * This constructor creates an object with the following parameters
	 * @param context is the context to work with
	 * @param whereId is the resource id where to place the string
	 * @param layoutId is the layout id of the list_view
	 * @param list is the list of dbobject's which are to putting in the list_view
	 */
	public MyPassaggioListAdapter(Context context, int whereId, int layoutId, Vector<Passaggio> list, int actpos) {
		this.context = context;
		this.list = list;
		this.layoutId = layoutId;
		this.whereId = whereId;
		this.actpos = actpos;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = vi.inflate(layoutId, null);
		if (list != null)
		{
			TextView textView = (TextView) v.findViewById(whereId);
			textView.setText(list.get(position).getOrario().format("%H:%M"));
			if(actpos + 1 == position)
			{
				textView.setBackgroundColor(this.context.getResources().getColor(R.color.sasa_orange));
			}
			else if(actpos < position)
			{
				//textView.setBackgroundColor(Color.rgb(0, 70, 0));
			}
			else if (actpos >= position)
			{
				//textView.setBackgroundColor(Color.rgb(70, 0, 0));
				textView.setTextColor(context.getResources().getColor(R.color.divider_background));
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
		return list.get(position).getId();
	}

}
