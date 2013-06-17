/**
 *
 * SASAbus.java
 * 
 * Created: Dec 4, 2011 12:32:09 PM
 * 
 * Copyright (C) 2011 Paolo Dongilli and Markus Windegger
 *
 * This file is part of SASAbus.

 * SASAbus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SASAbus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SASAbus.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.sasabz.android.sasabus;

import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;
import android.app.Application;
import android.content.Context;

public class SASAbus extends Application {
    private int dbDownloadAttempts;
    
    private static Context context = null;
 
    @Override
    public void onCreate() 
    {
        // Init values which could be loaded from files stored in res/raw
        setDbDownloadAttempts(0);
        super.onCreate();
        context = this.getApplicationContext();
    }

    @Override
    public void onTerminate()
    {
    	//do nothing
    	MySQLiteDBAdapter.closeAll();
    }
    
    /**
     * @return the Strings and Variables stored in the Context;
     */
    public static Context getContext()
    {
    	return context;
    }
    
	/**
	 * @param dbDownloadAttempts the dbDownloadAttempts to set
	 */
	public void setDbDownloadAttempts(int dbDownloadAttempts) {
		this.dbDownloadAttempts = dbDownloadAttempts;
	}

	/**
	 * @return the dbDownloadAttempts
	 */
	public int getDbDownloadAttempts() {
		return dbDownloadAttempts;
	}
 

}
