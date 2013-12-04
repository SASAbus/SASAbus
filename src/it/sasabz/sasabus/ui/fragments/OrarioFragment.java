/**
 *
 * ShowOrariActivity.java
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

package it.sasabz.sasabus.ui.fragments;

import java.util.ArrayList;
import it.sasabz.sasabus.R;
import it.sasabz.sasabus.data.models.Area;
import it.sasabz.sasabus.data.models.BusLine;
import it.sasabz.sasabus.data.models.BusStop;
import it.sasabz.sasabus.data.models.Itinerary;
import it.sasabz.sasabus.data.orm.ItineraryList;
import it.sasabz.sasabus.ui.SASAbus;
import it.sasabz.sasabus.ui.adapter.MyPassaggioListAdapter;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class OrarioFragment extends Fragment implements OnItemClickListener {


	
	//provides the linea for this object
	private BusLine linea;

	//provides the destination for this object
	private BusStop departure;
		
	//provides the departure in this object
	private BusStop  arrival;
	
	//provides the list for this object of all passages during the actual day
	private ArrayList<Itinerary> list = null;
	
	//is the next departure time of the bus
	private int pos;
	
	private Area bacino = null;

	private OrarioFragment() {
		
	}

	public OrarioFragment(Area bacino, BusLine linea, BusStop departure, BusStop arrival)
	{
		this();
		this.bacino = bacino;
		this.linea = linea;
		this.departure = departure;
		this.arrival = arrival;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.palina_listview_layout, container, false);
		TextView titel = (TextView)result.findViewById(R.id.untertitel);
		
		titel.setText(R.string.show_orari);
		
		Resources res = getResources();
		
		TextView lineat = (TextView)result.findViewById(R.id.line);
        TextView from = (TextView)result.findViewById(R.id.from);
        TextView to = (TextView)result.findViewById(R.id.to);
        
        lineat.setText(res.getString(R.string.line_txt) + " " + linea.toString());
        from.setText(res.getString(R.string.from) + " " + departure.toString());
        to.setText(res.getString(R.string.to) + " " + arrival.toString());
		
		fillData(result);
		if (pos != -1) {
			((ListView)result.findViewById(android.R.id.list)).setSelection(pos);
		}
		return result;
	}
	

	

	/**
	 * fills the listview with the timetable
	 * @return a cursor to the time table
	 */
	private void fillData(View result) {
		list = ItineraryList.getVector(linea.getId(), arrival.getName_de(), departure.getName_de(), bacino.getTable_prefix());
		pos = getNextTimePosition(list);
        MyPassaggioListAdapter orari = new MyPassaggioListAdapter(SASAbus.getContext(), R.id.text, R.layout.standard_row, list, pos);
        ListView listview = (ListView)result.findViewById(android.R.id.list);
        listview.setAdapter(orari);
        listview.setOnItemClickListener(this);
        listview.setDividerHeight(0);
        listview.setDivider(null);
	}

	/**
	 * This method gets the next departure time and returns the
	 * index of this element
	 * @param c is the cursor to the list_view
	 * @return the index of the next departure time
	 */
	private int getNextTimePosition(ArrayList<Itinerary> list2) {
		int count = list2.size();
		if (count == 0) {
			return -1;
		} else if (count == 1) {
			return 0;
		} else {
			int i = 0;
			boolean found = false;
			while (i <= count-2 && !found) {
				Time currentTime = new Time();
				Time sasaTime = new Time();
				Time sasaTimeNext = new Time();
				currentTime.setToNow();
				sasaTime = list2.get(i).getTime();
				sasaTimeNext = list2.get(i + 1).getTime();

				if (sasaTime.after(currentTime)
						|| sasaTime.equals(currentTime)
						|| sasaTime.before(currentTime)
						&& (sasaTimeNext.equals(currentTime) || sasaTimeNext
								.after(currentTime)))
				{
					found = true;
				}
				else
				{
					i++;
				}
			}
			return i;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
		// TODO Auto-generated method stub
			Itinerary orario = list.get(position);
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			
			Fragment fragment = fragmentManager.findFragmentById(R.id.onlinefragment);
			if(fragment != null)
			{
				ft.remove(fragment);
			}
			fragment = new WayFragment(bacino, linea, arrival, orario);
			ft.add(R.id.onlinefragment, fragment);
			ft.addToBackStack(null);
			ft.commit();
			fragmentManager.executePendingTransactions();
			/*
			Intent showWay = new Intent(this, ShowWayActivity.class);
			showWay.putExtra("orario", orario);
			showWay.putExtra("destinazione", destinazione);
			showWay.putExtra("linea", linea);
			showWay.putExtra("bacino", bacino.getId());
			startActivity(showWay);
			*/
	}

	
}
