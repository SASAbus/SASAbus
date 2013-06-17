/**
 * 
 *
 * Passaggio.java
 * 
 * Created: 14.12.2011 16:34:43
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

import android.database.Cursor;
import android.text.format.Time;


/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class Passaggio extends DBObject {
	
	private int idPalina = 0;
	
	private int codCorsa = 0;
	
	private int progressivo = 0;
	
	private Time orario = null;

	
	/**
	 * Creates a new Object
	 */
	public Passaggio()
	{
		super();
	}
	
	/**
	 * Creates a new Object with the following parameters:
	 * @param id is the identifyer in the database
	 * @param idPalina is the id-code of the busstop
	 * @param codCorsa is the id-code of the course
	 * @param progressivo is the progressiv number which are course-related (1-2-3-4....etc)
	 * @param orario is the time when the bus starts from this busstop
	 */
	public Passaggio(int id, int idPalina, int codCorsa, int progressivo, Time orario)
	{
		super(id);
		this.setIdPalina(idPalina);
		this.setCodCorsa(codCorsa);
		this.setProgressivo(progressivo);
		this.setOrario(orario);
	}
	
	/**
	 * Creates a new Object with the following parameters:
	 * @param id is the identifyer in the database
	 * @param idPalina is the id-code of the busstop
	 * @param codCorsa is the id-code of the course
	 * @param progressivo is the progressiv number which are course-related (1-2-3-4....etc)
	 * @param orario is the time when the bus starts from this busstop
	 */
	public Passaggio(int id, int idPalina, int codCorsa, int progressivo, String orario)
	{
		super(id);
		this.setIdPalina(idPalina);
		this.setCodCorsa(codCorsa);
		this.setProgressivo(progressivo);
		this.setOrario(orario);
	}
	
	/**
	 * This constructor creates an object from the coursor on an object in the database
	 * @param c is the cursor to an object of the database
	 */
	public Passaggio(Cursor c)
	{
		super(c.getInt(c.getColumnIndex("id")));
		this.setIdPalina(c.getInt(c.getColumnIndex("palinaId")));
		this.setCodCorsa(c.getInt(c.getColumnIndex("corsaId")));
		this.setProgressivo(c.getInt(c.getColumnIndex("progressivo")));
		this.setOrario(c.getString(c.getColumnIndex("orario")));
	}
	
	/**
	 * @return the idPalina
	 */
	public int getIdPalina() {
		return idPalina;
	}

	/**
	 * @param idPalina the idPalina to set
	 */
	public void setIdPalina(int idPalina) {
		this.idPalina = idPalina;
	}

	/**
	 * @return the codCorsa
	 */
	public int getCodCorsa() {
		return codCorsa;
	}

	/**
	 * @param codCorsa the codCorsa to set
	 */
	public void setCodCorsa(int codCorsa) {
		this.codCorsa = codCorsa;
	}

	/**
	 * @return the progressivo
	 */
	public int getProgressivo() {
		return progressivo;
	}

	/**
	 * @param progressivo the progressivo to set
	 */
	public void setProgressivo(int progressivo) {
		this.progressivo = progressivo;
	}

	/**
	 * @return the orario
	 */
	public Time getOrario() {
		return orario;
	}

	/**
	 * @param orario the orario to set
	 */
	public void setOrario(Time orario) {
		this.orario = orario;
	}
	
	/**
	 * @param orario the orario to set
	 */
	public void setOrario(String orario) {
		this.orario = new Time();
		String [] split = orario.split(":");
		this.orario.setToNow();
		this.orario.minute = Integer.parseInt(split[1]);
		this.orario.hour = Integer.parseInt(split[0]);
		this.orario.second = 0;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof Passaggio)
		{
			return false;
		}
		Passaggio pas = (Passaggio)o;
		if(pas.getIdPalina() != this.getIdPalina() || pas.getCodCorsa() != this.getCodCorsa() || pas.getOrario().equals(this.getOrario()))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return (this.getOrario().format("%H:%M")).trim();
	}
	
}
