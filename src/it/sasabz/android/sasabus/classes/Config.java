/**
 * 
 *
 * Config.java
 * 
 * Created: 14.12.2011 12:55:53
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
 * This class gets some special data from the databases used for some operations
 * in the application like the validationcheck etc...
 * 
 */
package it.sasabz.android.sasabus.classes;

import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;
import android.database.Cursor;


public class Config {
	
	
	 /**
     * Return a String containing the db validity start date
     * 
     * @return String containing a date in format YYYY-MM-DD
     */
    public static String getStartDate() {
    	MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
    	Cursor c = sqlite.rawQuery("select da_data from validita", null);
    	String date = null;
    	if(c.moveToFirst())
    		date = c.getString(c.getColumnIndex("da_data"));
    	c.close();
    	sqlite.close();
    	return date;
    }

    /**
     * Return a String containing the db validity end date
     * 
     * @return String containing a date in format YYYY-MM-DD
     */
    public static String getEndDate() {
    	MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
    	Cursor c = sqlite.rawQuery("select a_data from validita", null);
    	String date = null;
    	if(c.moveToFirst())
    		date = c.getString(c.getColumnIndex("a_data"));
        c.close();
        sqlite.close();
        return date;
    }
	
}
