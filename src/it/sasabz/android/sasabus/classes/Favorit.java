/**
 *
 * Favorit.java
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
import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Favorit {
	
	private int id = 0;
	
	private String partenza = null;
	
	private String destinazione = null;
	
	public Favorit()
	{
		
	}
	
	public Favorit(String partenza_de, String destinazione_de)
	{
		this.partenza = partenza_de;
		this.destinazione = destinazione_de;
	}
	
	public Favorit(int id, String partenza_de, String destinazione_de)
	{
		this.id = id;
		this.partenza = partenza_de;
		this.destinazione = destinazione_de;
	}

	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	

	/**
	 * @return the partenza_de
	 */
	public String getPartenza() {
		return partenza;
	}
	
	public String getPartenzaString() {
		String ret = "";
		String fromItalienisch = getPartenza().substring(0, getPartenza().indexOf("-")).trim();
		Palina fromStation = PalinaList.getTranslation(fromItalienisch, "it");
		if(fromStation != null)
		{
			ret = fromStation.toString(); 
		}
		return ret;
	}

	/**
	 * @param partenza_de the partenza_de to set
	 */
	public void setPartenza(String partenza) {
		this.partenza = partenza;
	}

	public String getDestinazioneString() {
		String ret = "";
		String toItalienisch = getDestinazione().substring(0, getDestinazione().indexOf("-")).trim();
		Palina toStation = PalinaList.getTranslation(toItalienisch, "it");
		if(toStation != null)
		{
			ret = toStation.toString(); 
		}
		return ret;
	}
	
	/**
	 * @return the destinazione_de
	 */
	public String getDestinazione() {
		return destinazione;
	}

	/**
	 * @param destinazione_de the destinazione_de to set
	 */
	public void setDestinazione(String destinazione) {
		this.destinazione = destinazione;
	}
	
	@Override
	public String toString()
	{
		String ret = "";
		Resources res = SASAbus.getContext().getResources();
		String toItalienisch = getDestinazione().substring(0, getDestinazione().indexOf("-")).trim();
		String fromItalienisch = getPartenza().substring(0, getPartenza().indexOf("-")).trim();
		Palina toStation = PalinaList.getTranslation(toItalienisch, "it");
		Palina fromStation = PalinaList.getTranslation(fromItalienisch, "it");
		if(toStation != null && fromStation != null)
		{
			ret = res.getString(R.string.from) + " " + fromStation.toString() + " - " + 
					res.getString(R.string.to) + " " + toStation.toString(); 
		}
		return ret;
	}
	
	
	public boolean insert(SQLiteDatabase db)
	{
		if(this.partenza == null || this.destinazione == null)
			return false;
		if(db == null || !db.isOpen() || db.isReadOnly())
			return false;
		String query_select = "Select * from " + FavoritenDB.FAVORITEN_TABLE_NAME + " where " +
				"partenza=\"" + partenza + "\" and destinazione=\"" + destinazione + "\";";
		Cursor cursor = db.rawQuery(query_select,null);
		if(cursor.getCount() != 0)
			return false;
		String query = "Insert into " + FavoritenDB.FAVORITEN_TABLE_NAME + 
				" VALUES ((select count(*) from " + FavoritenDB.FAVORITEN_TABLE_NAME + "), " +
						"\"" + partenza + "\", \"" + destinazione + "\");";
		db.execSQL(query);
		return true;
	}
	
	public boolean delete(SQLiteDatabase db)
	{
		if(db == null || !db.isOpen() || db.isReadOnly())
		{
			Log.v("FAVORITENLOESCHEN", "DB");
			return false;	
		}
		String query_select = "delete from " + FavoritenDB.FAVORITEN_TABLE_NAME + " where id=" + this.id + ";";
		Log.v("SQL_QUERY", query_select);
		db.execSQL(query_select);
		return true;
	}
}
