/**
 * 
 *
 * SelectPalinaLocationActivity.java
 * 
 * Created: 14.12.2011 19:04:53
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
package it.sasabz.android.sasabus;

import it.sasabz.android.sasabus.classes.PalinaList;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class SelectPalinaLocationActivity extends ListActivity{

    
    public SelectPalinaLocationActivity() {
    }

    /** Called with the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        /*
		bacino = null;
		linea = null;
		destinazione = null;
		if (extras != null) {
			bacino = extras.getString("bacino");
			linea = extras.getString("linea");
			destinazione = extras.getString("destinazione");
		}
		*/

        setContentView(R.layout.select_palina_layout);
        fillData();
    }

    /**
     * Called when the activity is about to start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        TextView textView = (TextView) v.findViewById(R.id.palina);
       // palina = textView.getText().toString();
        textView = (TextView) v.findViewById(R.id.progressivo);
        //progressivo = textView.getText().toString();
    	//Intent showOrario = new Intent(this, ShowOrariActivity.class);
    	//showOrario.putExtra("bacino", bacino);
    	//showOrario.putExtra("linea", linea);
    	//showOrario.putExtra("destinazione", destinazione);
    	//showOrario.putExtra("palina", palina);
    	//showOrario.putExtra("progressivo", progressivo);
    	//startActivity(showOrario);
    }

    
    private void fillData() {
        // Get all 'paline' from the database and create the item list
    	//Cursor c = mDbHelper.fetchPaline(bacino, linea, destinazione);
    	final Location aktloc = null;
    	
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	
    	
    	Cursor c = PalinaList.getCursorGPS(loc);
        startManagingCursor(c);

        String[] from = new String[] { "progressivo", "_id", "luogo" };
        int[] to = new int[] { R.id.progressivo, R.id.palina, R.id.luogo };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter paline =
            new SimpleCursorAdapter(this, R.layout.paline_row, c, from, to);
        setListAdapter(paline);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //menu.add(...);  // specific to this activity
        SharedMenu.onCreateOptionsMenu(menu);
        return true;
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case SharedMenu.MENU_ABOUT:
			{
				new About(this).show();
				return true;
			}
			case SharedMenu.MENU_TEST:
			{
				Intent selLinea = new Intent(this, SelectLineaActivity.class);
				selLinea.putExtra("bacino", "Merano-Meran");
				startActivity(selLinea);
				return true;
			}
		}
		return false;
	}
}