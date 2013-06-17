/**
 *
 * FavoritenDB.java
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
package it.sasabz.android.sasabus.classes;

import it.sasabz.android.sasabus.R;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritenDB extends SQLiteOpenHelper{

	 public static final int DATABASE_VERSION = 1;
	
	 public static final String FAVORITEN_TABLE_NAME = "favoriten";
	 
	 private static final String FAVORITEN_TABLE_CREATE =
	                "CREATE TABLE " + FAVORITEN_TABLE_NAME + "(" +
	                		"id INT AUTO_INCREMENT, " +
	                		"partenza TEXT, " +
	                		"destinazione TEXT );";
	
	
	
	public FavoritenDB(Context context) {
		super(context, context.getResources().getString(R.string.favoriten_db_name), null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(FAVORITEN_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("delete from " + FAVORITEN_TABLE_NAME + ";");
		db.execSQL("drop table" + FAVORITEN_TABLE_NAME + ";");
	}

}
