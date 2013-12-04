/**
 *
 * MapViewActivity.java
 *
 * Created: Mar 15, 2012 22:40:06 PM
 *
 * Copyright (C) 2012 Paolo Dongilli and Markus Windegger
 *
 * This file is part of SasaBus.

 * SasaBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SasaBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus. If not, see <http://www.gnu.org/licenses/>.
 *
<<<<<<< HEAD
 * This activity provides a map an the possibility to show a list of
 * bus stops which were contained in a "journey" (from - to)
 *
=======
>>>>>>> 9953b151ccb50fe6b852c8ea73cf2811caaf01a1
 */

package it.sasabz.sasabus.ui.map;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.rendertheme.InternalRenderTheme;
import org.mapsforge.core.GeoPoint;

import it.sasabz.android.sasabus.R;
import it.sasabz.sasabus.data.models.Area;
import it.sasabz.sasabus.data.models.BusLine;
import it.sasabz.sasabus.data.models.BusStop;
import it.sasabz.sasabus.data.models.Itinerary;
import it.sasabz.sasabus.data.orm.AreaList;
import it.sasabz.sasabus.data.orm.BusLineList;
import it.sasabz.sasabus.data.orm.BusStopList;
import it.sasabz.sasabus.data.orm.ItineraryList;
import it.sasabz.sasabus.ui.dialogs.About;
import it.sasabz.sasabus.ui.dialogs.Credits;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MapViewActivity extends MapActivity {

	// provides the linea for this object
	private int partenza = -1;

	// provides the destination for this object
	private int destinazione = -1;

	// provides the lineaid for this object
	private int linea = -1;

	private Area bacino = null;

	// provides the orarioId for this object
	private int orarioId = -1;
	

	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		partenza = 0;
		destinazione = 0;
		int bacinonr = 0;
		if (extras != null)
		{
			partenza = extras.getInt("partenza");
			destinazione = extras.getInt("destinazione");
			linea = extras.getInt("line");
			orarioId = extras.getInt("orarioId");
			bacinonr = extras.getInt("bacino");
		}
		else
		{
			Log.v("PECH", "PECH KOPP");
		}

		BusStop part = BusStopList.getBusStopById(partenza);
		part.setId(partenza);
		BusStop dest = BusStopList.getBusStopById(destinazione);
		dest.setId(destinazione);
		
		if (part == null || dest == null)
		{
			Toast.makeText(this, "ERROR partenza: " + partenza + " | destin: " + destinazione, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		bacino = AreaList.getById(bacinonr);
		BusLine line = BusLineList.getBusLineById(linea, bacino.getTable_prefix());

		Resources res = getResources();

		Itinerary pas = ItineraryList.getById(orarioId,
				bacino.getTable_prefix());

		if (part == null || dest == null || line == null || pas == null)
		{
			Toast.makeText(this, res.getString(R.string.error_application),
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		setContentView(R.layout.mapview_show_layout);
		TextView titel = (TextView) findViewById(R.id.titel);
		titel.setText(R.string.map);

		TextView lineat = (TextView) findViewById(R.id.line);
		TextView from = (TextView) findViewById(R.id.from);
		TextView to = (TextView) findViewById(R.id.to);

		if (lineat == null || from == null || to == null)
		{
			Toast.makeText(this, R.string.error_application, Toast.LENGTH_LONG)
					.show();
			finish();
			return;
		}

		lineat.setText(res.getString(R.string.line) + " " + line.toString());
		from.setText(res.getString(R.string.from) + " " + part.toString());
		to.setText(res.getString(R.string.to) + " " + dest.toString());

		
		/*
		 * Creating the MapView an the overlays to show the journay on the map.
		 */
		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setMapFile(new File(Environment.getExternalStorageDirectory(),
				res.getString(R.string.db_dir) + "/"
						+ res.getString(R.string.app_name_osm) + ".map"));
		mapView.setRenderTheme(InternalRenderTheme.OSMARENDER);

		GeoPoint partPoint = new GeoPoint(part.getLatitude(),
				part.getLongitude());
		GeoPoint destPoint = new GeoPoint(dest.getLatitude(),
				dest.getLongitude());

		Drawable start = getResources().getDrawable(R.drawable.ab_punkt);
		Drawable stop = getResources().getDrawable(R.drawable.ab_punkt);

		MyOverlayItem partOverlay = new MyOverlayItem(partPoint,
				res.getString(R.string.start), part.toString(), start);
		MyOverlayItem destOverlay = new MyOverlayItem(destPoint,
				res.getString(R.string.ziel), dest.toString(), stop);

		MyArrayItemizedOverlay arr = new MyArrayItemizedOverlay(start);
		MyArrayItemizedOverlay dest_arr = new MyArrayItemizedOverlay(stop);

		arr.addItem(partOverlay);
		dest_arr.addItem(destOverlay);

		mapView.getOverlays().add(arr);
		mapView.getOverlays().add(dest_arr);

		ArrayList<Itinerary> paslist = ItineraryList.getWay(orarioId,
				dest.getName_de(), bacino.getTable_prefix());

		Iterator<Itinerary> iter = paslist.iterator();

		Drawable inter = getResources().getDrawable(
				R.drawable.glyphicons_238_pin);

		MyArrayItemizedOverlay intermediate = new MyArrayItemizedOverlay(inter);

		while (iter.hasNext())
		{
			Itinerary passa = iter.next();
			BusStop pal = BusStopList.getBusStopById(passa.getBusStopId());
			pal.setId(passa.getBusStopId());
			if (pal.getId() != dest.getId() && pal.getId() != part.getId())
			{
				GeoPoint point = new GeoPoint(pal.getLatitude(),
						pal.getLongitude());
				MyOverlayItem overlay = new MyOverlayItem(point,
						res.getString(R.string.zwischenstop), pal.toString(),
						inter);
				intermediate.addItem(overlay);
			}
		}

		mapView.getOverlays().add(intermediate);

		mapView.setCenter(partPoint);

		mapView.getController().setZoom(14);

	}

}