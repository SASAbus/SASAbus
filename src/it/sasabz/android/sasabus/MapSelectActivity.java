/**
 *
<<<<<<< HEAD
 * MapSelectActivity.java
=======
 * MapViewActivity.java
>>>>>>> 9953b151ccb50fe6b852c8ea73cf2811caaf01a1
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
 * This activity provides a map to select one of the various bus stops
 *
=======
>>>>>>> 9953b151ccb50fe6b852c8ea73cf2811caaf01a1
 */

package it.sasabz.android.sasabus;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.android.maps.rendertheme.InternalRenderTheme;
import org.mapsforge.core.GeoPoint;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.classes.MyArrayItemizedSelectOverlay;
import it.sasabz.android.sasabus.classes.MyOverlaySelectItem;
import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.DBObject;
import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.R.drawable;
import it.sasabz.android.sasabus.R.id;
import it.sasabz.android.sasabus.R.layout;
import it.sasabz.android.sasabus.R.menu;
import it.sasabz.android.sasabus.R.string;
import it.sasabz.android.sasabus.classes.MyArrayItemizedOverlay;
import it.sasabz.android.sasabus.classes.MyArrayItemizedSelectOverlay;
import it.sasabz.android.sasabus.classes.MyOverlayItem;
import it.sasabz.android.sasabus.classes.MyOverlaySelectItem;
import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;
import it.sasabz.android.sasabus.classes.dbobjects.Bacino;
import it.sasabz.android.sasabus.classes.dbobjects.BacinoList;
import it.sasabz.android.sasabus.classes.dbobjects.DBObject;
import it.sasabz.android.sasabus.classes.dbobjects.Linea;
import it.sasabz.android.sasabus.classes.dbobjects.LineaList;
import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.classes.dbobjects.Passaggio;
import it.sasabz.android.sasabus.classes.dbobjects.PassaggioList;
import it.sasabz.android.sasabus.classes.dialogs.About;
import it.sasabz.android.sasabus.classes.dialogs.Credits;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MapSelectActivity extends MapActivity {
	
	private String from = "";
	
	private String to = "";
	
	/**
	 * sets the text from the textview from to the string from
	 * @param from is the string to set
	 */
	public void setFrom(String from)
	{
		this.from = from;
	}
	
	/**
	 * sets the text from textview to to the string to
	 * @param to is the string to set
	 */
	public void setTo(String to)
	{
		this.to = to;
	}
	
	
	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.mapview_select_layout);
		TextView titel = (TextView) findViewById(R.id.titel);
		titel.setText(R.string.map);

		Resources res = getResources();
		
		MapView mapView = (MapView) findViewById(R.id.mapView);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapView.setMapFile(new File(Environment.getExternalStorageDirectory(),
				res.getString(R.string.db_dir) + "/"
						+ res.getString(R.string.app_name_osm) + ".map"));
		mapView.setRenderTheme(InternalRenderTheme.OSMARENDER);

		Vector<DBObject> pallist = PalinaList.getMapList();
		
		Iterator<DBObject> iter = pallist.iterator();

		Drawable stop = getResources().getDrawable(
				R.drawable.glyphicons_238_pin);

		MyArrayItemizedSelectOverlay intermediate = new MyArrayItemizedSelectOverlay(stop);

		GeoPoint partPoint = null;
		
		while (iter.hasNext())
		{
			Palina pal = (Palina)iter.next();
			GeoPoint point = new GeoPoint(pal.getLatitude(),
					pal.getLongitude());
			if(partPoint == null)
			{
				partPoint = point;
			}
			MyOverlaySelectItem overlay = new MyOverlaySelectItem(point, stop, this, pal);
			intermediate.addItem(overlay);
		}

		mapView.getOverlays().add(intermediate);

		LocationManager locman = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastloc = locman.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(MySQLiteDBAdapter.exists(this))
        {
	        if(lastloc == null)
	        {
	        	lastloc = locman.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	        }
	        if(lastloc != null)
	        {
	        	try
	        	{
	        		Palina palina = PalinaList.getPalinaGPS(lastloc);
	        		if(palina != null)
	        		{
	        			partPoint = new GeoPoint(palina.getLatitude(), palina.getLongitude());
	        		}
	        	}
	        	catch(Exception e)
	        	{
	        		Log.e("HomeActivity", "Fehler bei der Location", e);
	        	}
	        }
	        else
	        {
	        	Log.v("HomeActivity", "No location found!!");
	        }
        }
		
		if(partPoint != null)
			mapView.setCenter(partPoint);

		mapView.getController().setZoom(15);

	}

	/**
	 * Called when the activity is about to start interacting with the user.
	 */
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	setResult();
           return true;
        }
        return false;
    }

	
	private void setResult()
	{
		Intent returnIntent = new Intent();
		returnIntent.putExtra("from", from);
		returnIntent.putExtra("to", to);
		setResult(Activity.RESULT_OK,returnIntent);
		finish();
	}
	
	@Override
	protected void onDestroy ()
	{
		super.onDestroy();
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
		switch (item.getItemId())
		{
		case R.id.menu_about:
		{
			new About(this).show();
			return true;
		}
		case R.id.menu_credits:
		{
			new Credits(this).show();
			return true;
		}
		}
		return false;
	}
	
	

}