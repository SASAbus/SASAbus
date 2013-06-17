/**
 *
 * MyListAdapter.java
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

import it.sasabz.android.sasabus.classes.dbobjects.DBObject;

import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 * 
 */
public class MyAutocompleteAdapter extends BaseAdapter implements Filterable {
	private final Context context;
	private final Vector<DBObject> origlist;
	private Vector<DBObject> datalist = new Vector<DBObject>();
	private final int layoutId;

	/**
	 * This constructor creates an object with the following parameters
	 * 
	 * @param context
	 *            is the context to work with
	 * @param whereId
	 *            is the resource id where to place the string
	 * @param layoutId
	 *            is the layout id of the list_view
	 * @param list
	 *            is the list of dbobject's which are to putting in the
	 *            list_view
	 */
	public MyAutocompleteAdapter(Context context, int layoutId,
			Vector<DBObject> list) {
		this.context = context;
		this.origlist = list;
		this.layoutId = layoutId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null)
		{
			LayoutInflater vi = LayoutInflater.from(context);
			v = vi.inflate(layoutId, null);
		}
		TextView textView = (TextView) v.findViewById(android.R.id.text1);
		textView.setTextColor(context.getResources().getColor(android.R.color.black));
		if (datalist != null)
		{
			DBObject listItem = datalist.get(position);
			if (listItem != null)
			{
				textView.setText(datalist.get(position).toString());
			}
		}
		return v;
	}

	@Override
	public int getCount() {
		if (datalist == null)
			return 0;
		return datalist.size();
	}

	@Override
	public Object getItem(int position) {
		if (datalist == null || position >= datalist.size())
			return null;
		return datalist.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (datalist == null)
			return -1;
		return datalist.get(position).getId();
	}

	@Override
	public Filter getFilter() {
		Filter myFilter = null;
		myFilter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();
				Vector<DBObject> temp_datalist = new Vector<DBObject>();
				if (constraint != null)
				{
					Iterator<DBObject> iter = origlist.iterator();
					String[] constraints = constraint.toString().split(" ");
					while (iter.hasNext())
					{
						DBObject object = iter.next();
						String s = object.toString();
						boolean match = false;
						for (int i = 0; i < constraints.length
								&& (match || i == 0); ++i)
						{
							match = s
									.toLowerCase(Locale.getDefault())
									.contains(
											constraints[i]
													.toString()
													.toLowerCase(
															Locale.getDefault()));
						}
						if (match)
						{
							temp_datalist.add(object);
						}
					}
				}
				filterResults.values = temp_datalist;
				filterResults.count = temp_datalist.size();
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence contraint,
					FilterResults results) {
				try
				{
					if (results.values instanceof Vector<?>)
						datalist = (Vector<DBObject>) results.values;
					notifyDataSetChanged();
				} 
				catch (Exception e)
				{
					Log.v("MyAutocompleteAdapter", "Error", e);
				}
			}
		};
		return myFilter;
	}
}
