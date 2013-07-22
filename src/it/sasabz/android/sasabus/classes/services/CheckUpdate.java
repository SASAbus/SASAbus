package it.sasabz.android.sasabus.classes.services;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


import it.sasabz.android.sasabus.CheckDatabaseActivity;
import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.Config;
import it.sasabz.android.sasabus.classes.MD5Utils;
import it.sasabz.android.sasabus.classes.network.SasabusFTP;
import it.sasabz.android.sasabus.fragments.OnlineSearchFragment;
import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class CheckUpdate extends AsyncTask<Void, String, Long> {

	private final OnlineSearchFragment activity;
	
	private final String TAG = "CheckUpdate";

	public CheckUpdate(OnlineSearchFragment activity) {
		super();
		this.activity = activity;
	}

	@Override
	protected Long doInBackground(Void... params) {
		SASAbus config = (SASAbus) activity.getActivity().getApplicationContext();
		Resources res = activity.getResources();
		String dbDirName = res.getString(R.string.db_dir);
		String dbFileName = res.getString(R.string.app_name_db) + ".db";
		String md5dbFileName = dbFileName + ".md5";
		// Check if the sd-card is mounted
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return Long.valueOf(OnlineSearchFragment.NO_SD_CARD);
		}
		File dbDir = new File(Environment.getExternalStorageDirectory(),
				dbDirName);
		// check if dbDir exists; if not create it
		if (!dbDir.exists()) {
			dbDir.mkdirs();
			return Long.valueOf(OnlineSearchFragment.DOWNLOAD_FILES);
		}
		// creates all files (zip, md5 and db)
		File dbFile = new File(dbDir, dbFileName);
		File md5dbFile = new File(dbDir, md5dbFileName);
		boolean download = false;
		if (dbFile.exists() && md5dbFile.exists()) {
			/*
			 * checks if the md5-sum are equal if not, directly download is
			 * true, whe have to do an update else we are checking other
			 * properties to download new database or not
			 */
			if (!MD5Utils.checksumOK(dbFile, md5dbFile)) {
				download = true;
			} else {
				String end = null;
				try {
					end = Config.getEndDate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				try {
					Date endDate = timeFormat.parse(end);
					Date currentDate = timeFormat.parse(timeFormat.format(cal
							.getTime()));
					Log.v("CheckDatabaseActivity",
							"endDate: " + endDate.toString()
									+ "; currentDate: "
									+ currentDate.toString());
					if (currentDate.after(endDate)) {
						download = true;
						config.setDbDownloadAttempts(config
								.getDbDownloadAttempts() + 1);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!download && fileUpdateAvailable(md5dbFileName, dbDir)) {
					download = true;
				}
			}
		} else {
			download = true;
			return Long.valueOf(OnlineSearchFragment.DOWNLOAD_FILES);
		}
		if(download)
			return Long.valueOf(OnlineSearchFragment.DOWNLOAD_AVAILABLE);
		
		
		dbDirName = res.getString(R.string.db_dir);
		dbFileName = res.getString(R.string.app_name_osm) + ".map";
		md5dbFileName = dbFileName + ".md5";

		// Check if the sd-card is mounted
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return Long.valueOf(CheckDatabaseActivity.NO_SD_CARD);
		}

		// creates all files (zip, md5 and db)
		dbFile = new File(dbDir, dbFileName);
		md5dbFile = new File(dbDir, md5dbFileName);

		download = false;
		if (dbFile.exists() && md5dbFile.exists()) {
			/*
			 * checks if the md5-sum are equal if not, directly download is
			 * true, whe have to do an update else we are checking other
			 * properties to download new database or not
			 */
			if (!MD5Utils.checksumOK(dbFile, md5dbFile)) {
				download = true;
			} else {
				String end = null;
				try {
					end = Config.getEndDate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				try {
					Date endDate = timeFormat.parse(end);
					Date currentDate = timeFormat.parse(timeFormat.format(cal
							.getTime()));
					Log.v("CheckDatabaseActivity",
							"endDate: " + endDate.toString()
									+ "; currentDate: "
									+ currentDate.toString());
					if (currentDate.after(endDate)) {
						download = true;
						config.setDbDownloadAttempts(config
								.getDbDownloadAttempts() + 1);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!download && fileUpdateAvailable(md5dbFileName, dbDir)) {
					download = true;
				}
			}
		} else {
			download = true;
			return Long.valueOf(OnlineSearchFragment.DOWNLOAD_FILES);
		}
		if(download)
			return Long.valueOf(OnlineSearchFragment.DOWNLOAD_AVAILABLE);
		return Long.valueOf(OnlineSearchFragment.DB_OK);
		
	}
	
	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		activity.showDialog(result.intValue(), OnlineSearchFragment.DB_UP);

	}

	/**
	 * this method controlls if a db-update is available. downloads the md5-file
	 * of the server and checks if the md5 is a new md5. when it is, then
	 * returns true, else false
	 * 
	 * @param md5FileName
	 *            is the filename of the md5-file on the server
	 * @param dbDir
	 *            is the local dirname to put into the downloaded md5
	 * @return a boolean to determinate if an update is necessary or not
	 */
	public boolean fileUpdateAvailable(String md5FileName, File dbDir) {
		boolean update = false;
		File md5File = new File(dbDir, md5FileName);
		long lastLocalMod = md5File.lastModified();
		Date lastLocalModDate = new Date(lastLocalMod);
		String lastRemoteMod;
		Date lastRemoteModDate;
		Resources res = activity.getResources();

		// verify we have a network connection, otherwise act as no update is
		// available
		// and update remains false
		if (haveNetworkConnection()) {
			try {
				/*
				 * istanziate an object of the SasabusFTP, which provides the
				 * most important methods for connecting and getting files from
				 * an FTP server
				 */
				SasabusFTP ftp = new SasabusFTP();
				// SasabusHTTP http = new
				// SasabusHTTP(res.getString(R.string.http_repository_url));

				// connecting and login to the server
				ftp.connect(res.getString(R.string.repository_url), Integer
						.parseInt(res.getString(R.string.repository_port)));
				ftp.login(res.getString(R.string.ftp_user),
						res.getString(R.string.ftp_passwd));

				//
				lastRemoteMod = ftp.getModificationTime(md5FileName);
				ftp.disconnect();
				SimpleDateFormat simple = new SimpleDateFormat("yyyyMMddhhmmss");
				TimeZone utcZone = TimeZone.getTimeZone("UTC");
				simple.setTimeZone(utcZone);
				lastRemoteModDate = simple.parse(lastRemoteMod);
				// lastRemoteModDate = http.getModificationTime(md5FileName);
				// check if date of remote file is after date of local file
				update = lastRemoteModDate.after(lastLocalModDate);

				Log.v("CheckDatabaseActivity", "Date of local md5: "
						+ lastLocalModDate.toString());
				Log.v("CheckDatabaseActivity", "Date of remote md5: "
						+ lastRemoteModDate.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return update;
	}

	/**
	 * this method checks if a networkconnection is active or not
	 * 
	 * @return boolean if the network is reachable or not
	 */
	private boolean haveNetworkConnection() {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) (activity
				.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			// testing WIFI connection
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			// testing GPRS/EDGE/UMTS/HDSPA/HUSPA/LTE connection
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}
}
