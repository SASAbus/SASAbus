/**
 * 
 *
 * MyWayListAdapter.java
 * 
 * Created: 17.12.2011 15:10:39
 * 
 * Copyright (C) 2011 Paolo Dongilli & Markus Windegger
 * 
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
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.classes.dbobjects.Passaggio;

import java.util.Vector;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class MyWayListAdapter extends BaseAdapter {
	private final Context context;
	private final Vector<Passaggio> list;
	private final int actpos;

	
	/**
	 * This constructor creates an object with the following parameters
	 * @param context is the context to work with
	 * @param whereId is the resource id where to place the string
	 * @param layoutId is the layout id of the list_view
	 * @param list is the list of dbobject's which are to putting in the list_view
	 */
	public MyWayListAdapter(Context context, Vector<Passaggio> list, int actPos) {
		this.context = context;
		this.list = list;
		this.actpos = actPos;
	}

	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		LayoutInflater vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (position == 0) 
		{
			v = vi.inflate(R.layout.way_row_first, null);
		} 
		else if (position == list.size() - 1) 
		{
			v = vi.inflate(R.layout.way_row_end, null);
		}
		else 
		{
			v = vi.inflate(R.layout.way_row_middle, null);
		}
		if (list != null)
		{
			TextView departure = (TextView) v.findViewById(R.id.departure);
			Time currentTime = new Time();
			currentTime.setToNow();
			currentTime.set(0, currentTime.minute, currentTime.hour, currentTime.monthDay, currentTime.month, currentTime.year);
			Time sasaTime = list.get(position).getOrario();
			departure.setText(list.get(position).getOrario().format("%H:%M") + " "+ PalinaList.getById(list.get(position).getIdPalina()).toString());
			if (actpos < position)
			{
				//v.setBackgroundColor(Color.rgb(0, 70, 0));
				//departure.setTextColor()
			}
			else if (actpos > position)
			{
				//v.setBackgroundColor(Color.rgb(70, 0, 0));
				departure.setTextColor(context.getResources().getColor(R.color.divider_background));
			}
			else if(sasaTime.before(currentTime))
			{
				//v.setBackgroundColor(Color.rgb(70, 0, 0));
				departure.setTextColor(context.getResources().getColor(R.color.divider_background));
			}
			else if(sasaTime.after(currentTime))
			{
				//v.setBackgroundColor(Color.rgb(0, 70, 0));
			}
			else
			{
				//v.setBackgroundColor(Color.rgb(255, 125, 33));
				departure.setTextColor(context.getResources().getColor(R.color.sasa_orange));
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
