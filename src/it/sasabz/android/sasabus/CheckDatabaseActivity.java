/**
 *
 * CheckDatabaseActivity.java
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus. If not, see <http://www.gnu.org/licenses/>.
 *
 * This activity checks the actually loaded database if it's the newest one. The database is 
 * connecting to a FTP-server and checks the hashsum (md5-sum of the database). If an update is available,
 * a background service is loaded and the files were retrieved.
 */

package it.sasabz.android.sasabus;




import it.sasabz.android.sasabus.classes.services.FileRetriever;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

public class CheckDatabaseActivity extends Activity {

	
	/*
	 * Definition of the constants and the FileRetriever results to test if the file was 
	 * downloaded or an error occoured
	 */
	public final static int DOWNLOAD_SUCCESS_DIALOG = 0;
	public final static int DOWNLOAD_ERROR_DIALOG = 1;
	public final static int MD5_ERROR_DIALOG = 2;
	public final static int NO_NETWORK_CONNECTION = 3;
	public final static int NO_DB_UPDATE_AVAILABLE = 4;
	public final static int NO_SD_CARD = 5;
	public final static int DB_OK = 6;
	public final static int DOWNLOAD_RETRY = 7;
	
	/*
	 * Here for every file to retrieve a variable is defined
	 */
	//Variable for the OpenStreetMap-Mapfile
	public final static int FR_OSM = 0;
	//Variable of the database with the static data from SASA SpA-AG
	public final static int FR_DB = 1;
	

	
	
	/** Called with the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.splash);
		check_db();
	}
	
	/**
	 * in this method the database is been checked via a background service if it's the newest one or not.
	 */
	public void check_db()
	{
		Resources res = this.getResources();
		String db_filename = res.getString(R.string.app_name_db) + ".db";
		try {
			new FileRetriever(this, db_filename, res.getString(R.string.downloading_db), res.getString(R.string.unzipping_db)).execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * in this method the openstreepmap-mapfile is been checked via a background service
	 */
	public void check_osm()
	{
		Resources res = this.getResources();
		String osm_filename = res.getString(R.string.app_name_osm) + ".map";
		try {
			new FileRetriever(this, osm_filename, res.getString(R.string.downloading_map), res.getString(R.string.unzipping_map)).execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * This method a dialog have been sowed with a given id as title and a given res as message. 
	 * @param id is the returnvalue of the background service
	 * @param res is the id of the file (database or osm-mapfile)
	 */
	public void showDialog(int id, int res)
	{
		Dialog dia = onCreateDialog(id, res);
		dia.show();
	}

	
	/**
	 * This method creates an alert dialog to inform the user about what is ongoing, if 
	 * there occoured an error or if the file was retrieved successfully
	 * @param msg is the message to show in the dialog
	 * @param placeholder is the parameter for some string that contains an 
	 * @param res is the id of the threatened file
	 * @return
	 */
	public final Dialog createAlertDialog(int msg, String placeholder, final int res) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle(R.string.a_given_string);
		builder.setIcon(R.drawable.icon);
		//builder.setMessage(msg);
		builder.setMessage(String.format(getString(msg),placeholder));
		builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

			/**
			 * If the first file was checked/updated the the next file will be checked/updated.
			 * Otherwise a new activity is started via startActivity();
			 */
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if(res == FR_DB)
				{
					check_osm();
				}
				else
				{
					startActivity();
				}
				
			}
		});
		return builder.create();
	}
	

	/**
	 * this method is creating an allert message
	 * @param msg is the message to be shown in the alert dialog
	 * @return an Dialog to show
	 */
	private final Dialog createErrorAlertDialog(int msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle(R.string.a_given_string);
		builder.setIcon(R.drawable.icon);
		builder.setMessage(msg);
		builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {
				System.exit(0);
			}
		});
		return builder.create();
	}
	
	/**
	 * this method is creating an allert message
	 * @param msg is the message to be shown in the alert dialog
	 * @return an Dialog to show
	 */
	private final Dialog createRetryDialog(int msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// builder.setTitle(R.string.a_given_string);
		builder.setIcon(R.drawable.icon);
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				finish();
				Intent intent = new Intent(getContext(), CheckDatabaseActivity.class);
				startActivity(intent);
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int id) {
				finish();
				System.exit(-1);
			}
		});
		return builder.create();
	}

	private Context getContext()
	{
		return super.getApplicationContext();
	}
	
	
	/**
	 * Called when all downloads were successful and we have to start the
	 * first user activity called SelectModeActivity
	 */
	public void startActivity() {
		finish();
	}

	
	/**
	 * This method creates a dialog, depending on the id (returnvalue of the background-service)
	 * and the file for which the fileretriever as called
	 * @param id is the returnvalue of the background service
	 * @param res is the type of the checked/updated file
	 * @return a Dialog, not allready shown, but created and ready to show
	 */
	protected Dialog onCreateDialog(int id, int res) {
		switch (id) {
		case NO_NETWORK_CONNECTION:
			return createErrorAlertDialog(R.string.no_network_connection);
		case NO_DB_UPDATE_AVAILABLE:
			return createErrorAlertDialog(R.string.no_db_update_available);
		case DOWNLOAD_SUCCESS_DIALOG:
			String filename = getString(R.string.app_name_db)+ ".db";
			if(res == FR_OSM)
				filename = getString(R.string.app_name_osm)+ ".map";
			return createAlertDialog(R.string.db_ok,  filename, res);
		case DB_OK:
			String filename_ok = getString(R.string.app_name_db)+ ".db";
			if(res == FR_OSM)
				filename_ok = getString(R.string.app_name_osm)+ ".map";
			return createAlertDialog(R.string.db_ok,  filename_ok, res);
		case DOWNLOAD_ERROR_DIALOG:
			return createErrorAlertDialog(R.string.db_download_error);
		case MD5_ERROR_DIALOG:
			return createErrorAlertDialog(R.string.md5_error);
		case NO_SD_CARD:
			return createErrorAlertDialog(R.string.sd_card_not_mounted);
		case DOWNLOAD_RETRY:
			Log.v("CheckDatabaseActivity", "Habe retry Dialog erstellt!");
			return createRetryDialog(R.string.retry_download);
		default:
			return null;
		}
	}


	
}