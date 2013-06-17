/**
 *
 * Information.java
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

import it.sasabz.android.sasabus.classes.dbobjects.DBObject;

import java.io.UnsupportedEncodingException;
import java.util.Locale;


public class Information extends DBObject{
	
	
	private String titel_de;
	
	private String titel_it;
	
	private String nachricht_de;
	
	private String nachricht_it;
	
	private int stadt;
	
	
	public int getStadt() {
		return stadt;
	}


	public void setBacino(int stadt) {
		this.stadt = stadt;
	}


	public Information(int id, String titel_de, String titel_it, String nachricht_de, String nachricht_it, int stadt)
	{
		super(id);
		this.titel_de = titel_de;
		this.titel_it = titel_it;
		this.nachricht_de = nachricht_de;
		this.nachricht_it = nachricht_it;
		this.stadt = stadt;
	}
	

	public String getTitel_de() {
		return titel_de;
	}


	public void setTitel_de(String titel_de) {
		this.titel_de = titel_de;
	}


	public String getTitel_it() {
		return titel_it;
	}


	public void setTitel_it(String titel_it) {
		this.titel_it = titel_it;
	}


	public String getNachricht_de() {
		return nachricht_de;
	}


	public void setNachricht_de(String nachricht_de) {
		this.nachricht_de = nachricht_de;
	}


	public String getNachricht_it() {
		return nachricht_it;
	}


	public void setNachricht_it(String nachricht_it) {
		this.nachricht_it = nachricht_it;
	}
	
	public String getNachricht()
	{
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			return this.getNachricht_de().trim();
		}
		return this.getNachricht_it().trim();
	}
	
	public String getTitel()
	{
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			return this.getTitel_de().trim();
		}
		return this.getTitel_it().trim();
	}
	
	@Override
	public String toString()
	{
		try {
			return (new String(this.getTitel().getBytes(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
