/**
 *
 * MySQLiteDBAdapter.java
 * 
 * Created: Dez 13, 2011 15:30:08 PM
 * 
 * Copyright (C) 2011 Markus Windegger
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

package it.sasabz.android.sasabus.classes.adapter;

import it.sasabz.android.sasabus.R;
import it.sasabz.android.sasabus.classes.DBFileManager;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * This is an object designed with the idea of the singleton pattern
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class MySQLiteDBAdapter {
	
	private static SQLiteDatabase sqlite= null;
	private static DatabaseHelper helper = null;
	private static int counteropen = 0;
	private static int transactioncounter = 0;
	
	public static boolean exists(Context context)
	{
		String appName = context.getResources().getString(R.string.app_name_db);
		String dbFileName = appName + ".db";
		if(helper == null)
		{
			helper = new DatabaseHelper(dbFileName, null);
		}
		if(!helper.databaseFileExists())
			return false;
		return true;
	}
	
	/**
	 * This static method allows you tu getting an instance of the current database
	 * @param context is the actual context
	 * @return an opened and read only sqlite database
	 */
	public static MySQLiteDBAdapter getInstance(Context context)
	{
		if(counteropen == 0)
		{
			Resources res = context.getResources();
			String appName = res.getString(R.string.app_name_db);
			String dbFileName = appName + ".db";
			if(helper == null)
				helper = new DatabaseHelper(dbFileName,null);
			sqlite = helper.getReadableDatabase();
			if(sqlite == null)
			{
				System.err.println("Die Datenbank konnte nicht geoeffnet werden");
				System.exit(-2);
			}
		}
		++counteropen;
		return new MySQLiteDBAdapter();
	}
	
	/**
	 * makes the constructor private and so the only way to obtain an instance 
	 * of the object is the static method getInstance
	 */
	private MySQLiteDBAdapter()
	{
		//do nothing
	}
	
	/**
	 * This method closes all open MySQLiteDBAdapter
	 */
	public static void closeAll() 
	{
			helper.close();
			sqlite.close();
	}
	
	/**
	 * This method "closes" the database. There is a counter, because when there are more then one
	 * instance, then the db will not be closed, it will be decrementet only the counter.
	 * when the counter is 0, then the database will be closed
	 */
	public void close() 
	{
		--counteropen;
		if(counteropen == 0)
		{
			helper.close();
			sqlite.close();
		}
	}
	
	/**
	 * This method allows you to query the database
	 * @param query is the query to send
	 * @param args are the arguments for the query
	 * @return a cursor to the result set of the query
	 */
	public Cursor rawQuery(String query, String[] args)
	{
		Cursor ret = null;
		ret = sqlite.rawQuery(query, args);
		return ret;
	}
	
	/**
	 * to work with transaction management, and to provide it with the idea
	 * of the singleton pattern, there is a counter that counts the transactions open
	 * because the database can handle only one transaction open per dbfile
	 */
	public void beginTransaction()
	{
		if(transactioncounter == 0)
		{
			sqlite.beginTransaction();
		}
		transactioncounter++;
		
	}
	
	/**
	 * to work with transaction management, and to provide it with the idea
	 * of the singleton pattern, there is a counter that counts the transactions closed
	 * because the database can handle only one transaction open per dbfile
	 */
	public void endTransaction()
	{
		transactioncounter--;
		if(transactioncounter == 0)
		{
			sqlite.endTransaction();
		}
	}
	
	/**
	 * This classes helps to open the database read only and close it correctly after using
	 * @author Markus Windegger (markus@mowiso.com)
	 *
	 */
	private static class DatabaseHelper extends DBFileManager {

        DatabaseHelper(String dbFileName, SQLiteDatabase.CursorFactory factory) {
            super(dbFileName, factory);
        }
    }
	
}
