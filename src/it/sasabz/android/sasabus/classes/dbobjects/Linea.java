/**
 * 
 *
 * Linea.java
 * 
 * Created: 13.12.2011 19:36:47
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
 */
package it.sasabz.android.sasabus.classes.dbobjects;

import java.util.Locale;

import android.database.Cursor;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class Linea extends DBObject {
	
	
	private String num_lin = null;
	
	private String descr_it = null;
	
	private String descr_de = null;
	
	private String abbrev = null;
	
	private int differenza = -1;

	

	/**
	 * This is the standardconstructor, which make an empty object
	 */
	public Linea()
	{
		super();
	}
	
	/**
	 * this constructor creates an object from the standtart
	 * cursor to an entry in the database
	 * @param c is the curser to an entry in the database
	 */
	public Linea(Cursor c)
	{
		super(c.getInt(c.getColumnIndex("id")));
		this.setNum_lin(c.getString(c.getColumnIndex("num_lin")));
		this.setAbbrev(c.getString(c.getColumnIndex("abbrev")));
		this.setDescr_de(c.getString(c.getColumnIndex("descr_de")));
		this.setDescr_it(c.getString(c.getColumnIndex("descr_it")));
		if(c.getColumnIndex("differenza") != -1)
		{
			this.setDifferenza(c.getInt(c.getColumnIndex("differenza")));
		}
	}

	
	
	/**
	 * @return the descr_it
	 */
	public String getDescr_it() {
		return descr_it;
	}


	/**
	 * @param descr_it the descr_it to set
	 */
	public void setDescr_it(String descr_it) {
		this.descr_it = descr_it;
	}


	/**
	 * @return the descr_de
	 */
	public String getDescr_de() {
		return descr_de;
	}


	/**
	 * @param descr_de the descr_de to set
	 */
	public void setDescr_de(String descr_de) {
		this.descr_de = descr_de;
	}


	/**
	 * @return the abbrev
	 */
	public String getAbbrev() {
		return abbrev;
	}


	/**
	 * @param abbrev the abbrev to set
	 */
	public void setAbbrev(String abbrev) {
		this.abbrev = abbrev;
	}


	/**
	 * @return the num_lin
	 */
	public String getNum_lin() {
		return num_lin;
	}

	/**
	 * @param num_lin the num_lin to set
	 */
	public void setNum_lin(String num_lin) {
		this.num_lin = num_lin;
	}
	
	
	/**
	 * @return the num_lin
	 */
	public int getDifferenza() {
		return differenza;
	}

	/**
	 * @param num_lin the num_lin to set
	 */
	public void setDifferenza(int differenza) {
		this.differenza = differenza;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (object == null)
			return false;
		if(!(object instanceof Linea))
			return false;
		Linea linea = (Linea)object;
		if(linea.getId() != this.getId())
			return false;
		return true;
	}
	
	
	/**
	 * This function compares two Linea object and return their difference value
	 * @param linea is the line to compare with
	 * @return 0, if the two lines are equal, -1 if this line is less then, and 1 
	 * if this line is greater then the line to compare with
	 */
	public int compareTo(Linea linea) throws NumberFormatException
	{
		int sortnum = 0;
		boolean failure = false;
		boolean failure_lin = false;
		try {
			sortnum = Integer.parseInt(this.getNum_lin());
		}
		catch (NumberFormatException e)
		{
			try
			{
				sortnum = Integer.parseInt(this.getNum_lin().substring(0, this.getNum_lin().length()-1));
			}
			catch(Exception ex)
			{
				failure = true;
			}
			
		}
		int linsortnum = 0;
		try
		{
			linsortnum = Integer.parseInt(linea.getNum_lin());
		}
		catch (NumberFormatException e)
		{
			try
			{
				linsortnum = Integer.parseInt(linea.getNum_lin().substring(0, linea.getNum_lin().length()-1));
			}
			catch(Exception ex)
			{
				failure_lin = true;
			}
			
		}
		if(failure && failure_lin)
		{
			return this.getNum_lin().compareTo(linea.getNum_lin());
		}
		else if(failure)
		{
			return 1;
		}
		else if(failure_lin)
		{
			return -1;
		}
		if(sortnum == linsortnum)
			return 0;
		if (sortnum > linsortnum)
			return 1;
		return -1;
	}
	
	
	/**
	 * This toString takes control of the localized output
	 */
	@Override
	public String toString()
	{
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			return (this.getNum_lin()).trim();
		}
		return (this.getNum_lin()).trim();
	}
	
	
}
