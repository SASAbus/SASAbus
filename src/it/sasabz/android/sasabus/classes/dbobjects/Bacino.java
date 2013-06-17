/**
 *
 * Bacino.java
 * 
 * Created: Dez 13, 2011 16:12:03 PM
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

package it.sasabz.android.sasabus.classes.dbobjects;

import java.util.Locale;

import android.database.Cursor;

public class Bacino extends DBObject {
	
	/*
	 * Is the name of the current Bacino in italian and german
	 */
	private String bacino_de = null;
	
	private String bacino_it = null;
	
	private String table_prefix = null;
	
	
	
	
	/*
	 * The standard-constructor
	 */
	public Bacino() {
		super();
	}
	
	/**
	 * This constructor creates an object from the data id and the name of the bacino
	 * @param identifyer is the id from the database
	 * @param bacinoName is the name of the bacino
	 */
	public Bacino (int identifyer, String bacino_it, String bacino_de){
		super(identifyer);
		this.bacino_it = bacino_it;
		this.bacino_de = bacino_de;
	}
	
	/**
	 * This constructor creates an object from the database provinding directly the
	 * default cursor from the database
	 * @param c is the cursor on an object from the database
	 */
	public Bacino(Cursor c)
	{
		super(c.getInt(c.getColumnIndex("id")));
		this.setBacino_de(c.getString(c.getColumnIndex("nome_de")));
		this.setBacino_it(c.getString(c.getColumnIndex("nome_it")));
		this.setTable_prefix(c.getString(c.getColumnIndex("nome_table")));
	}
	
	/**
	 * @return the bacino_de
	 */
	public String getBacino_de() {
		return bacino_de;
	}

	/**
	 * @param bacino_de the bacino_de to set
	 */
	public void setBacino_de(String bacino_de) {
		this.bacino_de = bacino_de;
	}

	/**
	 * @return the bacino_it
	 */
	public String getBacino_it() {
		return bacino_it;
	}

	/**
	 * @param bacino_it the bacino_it to set
	 */
	public void setBacino_it(String bacino_it) {
		this.bacino_it = bacino_it;
	}

	public String getTable_prefix()
	{
		return this.table_prefix;
	}
	
	public void setTable_prefix(String table_prefix)
	{
		this.table_prefix = table_prefix;
	}
	
	/**
	 * This method returns the string related to the locale set on the smartphone 
	 */
	@Override
	public String toString()
	{
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			return this.getBacino_de().trim();
		}
		
		return this.getBacino_it().trim();
	}
	
}
