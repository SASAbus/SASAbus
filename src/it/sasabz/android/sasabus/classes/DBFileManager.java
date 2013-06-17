/**
 *
 * DBFileManager.java
 * 
 * Created: Jan 16, 2011 5:30:31 PM
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.sasabz.android.sasabus.classes;

import java.io.File;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.AndroidRuntimeException;

public abstract class DBFileManager {
	
	//providing name, file and sqlite databases
	private SQLiteDatabase database;
	private File dbFile;
	private SQLiteDatabase.CursorFactory factory;

	/**
	 * this constructor creates an object with the dbFileName and the actual factory
	 * @param dbFileName is the name of the db file
	 * @param factory is the actual factory of the activity
	 */
	public DBFileManager(String dbFileName,
			SQLiteDatabase.CursorFactory factory) {
		this.factory = factory;

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new AndroidRuntimeException(
					"External storage (SD-Card) not mounted");
		}
		File appDbDir = new File(Environment.getExternalStorageDirectory(),
				"Android/data/it.sasabz.android.sasabus/db");
		if (!appDbDir.exists()) {
			appDbDir.mkdirs();
		}
		this.dbFile = new File(appDbDir, dbFileName);
	}

	/**
	 * This method controls if the db-file exists
	 * @return true if the file exists, false otherwise
	 */
	public boolean databaseFileExists() {
		return dbFile.exists();
	}

	/**
	 * this method opens the db-file read_only
	 */
	private void open() {
		if (dbFile.exists()) {
			database = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(),
					factory, SQLiteDatabase.OPEN_READONLY);
		}
	}

	/**
	 * this closes the database
	 */
	public void close() {
		if (database != null) {
			database.close();
			database = null;
		}
	}

	/**
	 * this synchronized method returns a read_only database
	 * @return this database
	 */
	public synchronized SQLiteDatabase getReadableDatabase() {
		return getDatabase();
	}

	/**
	 * this method controls if the databases is already opened, otherwise
	 * the database getting opened and then returned
	 * @return the open, readonly database
	 */
	private SQLiteDatabase getDatabase() {
		if (database == null) {
			open();
		}
		return database;
	}
}