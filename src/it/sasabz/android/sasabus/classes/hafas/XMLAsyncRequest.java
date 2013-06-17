/**
 *
 * XMLAsyncRequest.java
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

package it.sasabz.android.sasabus.classes.hafas;


import it.sasabz.android.sasabus.SASAbus;

import it.sasabz.android.sasabus.R;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;



/**
 * FileDownloader is my own delegate class that performs the
 * actual downloading and is initialized with the source URL.
 * 
 * @author Paolo Dongilli and Markus Windegger
 *
 */
public class XMLAsyncRequest  extends AsyncTask<Void, String, String>{
	
	private static final String TAG = "XML-LOGGER";
	
	private ProgressDialog progressDialog;
	
	private String request = ""; 
	
	private Activity activity = null;

	/**
	 * This constructor takes an activity, a dbZipFile to store the 
	 * downloaded Zip file, a dbFile to store the unzipped database and at least
	 * a md5File to save the downloaded md5File
	 * @param activity is the actual activity
	 * @param dbZIPFile is the file to save the downloaded zip file
	 * @param dbFile is the file to save the unzipped db file
	 * @param md5File is the file to save the downloaded md5 file
	 */
	public XMLAsyncRequest(Activity activity, String request) {
		super();
		this.activity = activity;
		this.request = request;
	}
	
	/**
	 * @return the progressDialog
	 */
	public ProgressDialog getProgressDialog() {
		return progressDialog;
	}

	/**
	 * @param progressDialog the progressDialog to set
	 */
	public void setProgressDialog(ProgressDialog progressDialog) {
		this.progressDialog = progressDialog;
	}

	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(Void...params) {	
		String ret = "";
		if(!haveNetworkConnection())
		{
			return ret;
		}
		try {
			HttpClient http = new DefaultHttpClient();
			HttpPost post = new HttpPost(SASAbus.getContext().getString(R.string.xml_server));
			StringEntity se = new StringEntity(request, HTTP.UTF_8);
			se.setContentType("text/xml");
			post.setEntity(se);
			
			HttpResponse response = http.execute(post);
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				ret = EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
		
	
	
	/**
	 * this method checks if a networkconnection is active or not
	 * @return boolean if the network is reachable or not
	 */
	private boolean haveNetworkConnection() 
	{
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) (activity.getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			//testing WIFI connection
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			//testing GPRS/EDGE/UMTS/HDSPA/HUSPA/LTE connection
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
	
	@Override
	public void onProgressUpdate(String... args) {
		this.progressDialog.setProgress(Integer.parseInt(args[0]));
		progressDialog.show();
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		
		
		

	}
	
	
}