/**
 * 
 *
 * LineaList.java
 * 
 * Created: 13.12.2011 20:13:31
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

import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.Config;
import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;

import java.util.Vector;

import android.database.Cursor;

/**
 * @author Markus Windegger (markus@mowiso.com)
 *
 */
public class LineaList {
	
	
	
	/**                                                                                                                                                                                                          
	 * This function returns a vector of all the objects momentanly avaiable in the database                                                                                                                     
	 * @return a vector of objects if all goes right, alternativ it returns a MyError                                                                                                                              
	 */
	public static  Vector <DBObject>  getList()
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		Cursor cursor = sqlite.rawQuery("select * from linee", null);
		Vector <DBObject> list = null;
		if(cursor.moveToFirst())
		{
			list = new Vector<DBObject>();
			do {
				Linea element = new Linea(cursor);
				list.add(element);
			} while(cursor.moveToNext());
		}
		cursor.close();
		sqlite.close();
		return list;
	}
	
	/**
	 * This method returns a vector of linee which are located in the bacino 
	 * @param bacino is the bacino where we are searching the linee 
	 * @return a vector of DBObjects with the linee located in the bacino
	 */
	public static Vector <DBObject> getList(String table_prefix)
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		String[] args = null;
		Cursor cursor = sqlite.rawQuery("select * from " + table_prefix + "linee order by num_lin", args);
		Vector <DBObject> list = null;
		if(cursor.moveToFirst())
		{
			list = new Vector<DBObject>();
			do {
				Linea element = new Linea(cursor);
				list.add(element);
			} while(cursor.moveToNext());
		}
		cursor.close();
		sqlite.close();
		return list;
	}
	
	/**
	 * This method returns a vector of linee which are connecting the departure with the destination 
	 * @param destinazione is the name of the destination busstop
	 * @param partenza is the name of the departure busstop
	 * @return a vector of DBObjects which are lines which connect the departure with the destination
	 */
	public static Vector <DBObject> getListDestPart(String destinazione, String partenza)
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		String[] args = {destinazione, partenza};
		/*
		 * This is a special query to provide the cases which the departure is 
		 * yesterday near midnight and the destination is now, for showing non-negativ
		 * differences
		 */
		Cursor cursor = sqlite.rawQuery("select distinct l.id as id, l.num_lin as num_lin, l.abbrev as abbrev, " +
				"l.bacinoId as bacinoId, l.descr_it as descr_it, l.descr_de as descr_de " +
				" from linee l, " +
				"(select * from orarii where palinaId IN (" +
				"select id from paline where nome_de = ?" +
				")) as o1, " +
				"(select * from orarii where palinaId IN (" +
				"select id from paline where nome_de = ?" +
				")) as o2, " +
				"(select id, lineaId " +
				"from corse " +
				"where  " +
				"substr(corse.effettuazione,round(strftime('%J','now','localtime')) - round(strftime('%J', '" + Config.getStartDate() + "')) + 1,1)='1' " + 
				") as c " +
				"where c.lineaId = l.id " +
				"and o1.corsaId = o2.corsaId " +
				"and o2.corsaId = c.id " +
				"and o2.progressivo < o1.progressivo " +
				"order by l.abbrev", args);
		Vector <DBObject> list = null;
		if(cursor.moveToFirst())
		{
			list = new Vector<DBObject>();
			do {
				Linea element = new Linea(cursor);
				if(!list.contains(element))
					list.add(element);
			} while(cursor.moveToNext());
		}
		cursor.close();
		sqlite.close();
		return list;
	}
	
	
	public static Vector<DBObject> sort(Vector <DBObject> list)
	{
		int j, i;
		try {
			for (j=list.size(); j > 1;--j)
			{
				for(i=0;i < j-1;++i)
				{
					Linea current = (Linea)list.elementAt(i);
					Linea next = (Linea)list.elementAt(i+1);
					if(current.compareTo(next) > 0)
					{
						list.remove(i);
						list.add(i+1, current);
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**                                                                                                                                                                                                          
	 * This function returns a vector of all the objects momentanly avaiable in the database                                                                                                                     
	 * @return a vector of objects if all goes right, alternativ it returns a MyError                                                                                                                              
	 */
	public static  Linea  getById(int lineaId, String table_prefix)
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		String[] args = {Integer.toString(lineaId)};
		Cursor cursor = sqlite.rawQuery("select * from " + table_prefix + "linee where id = ?", args);
		Linea line = null;
		if(cursor.moveToFirst())
		{
			line = new Linea(cursor);
		}
		cursor.close();
		sqlite.close();
		return line;
	}
	
	/**                                                                                                                                                                                                          
	 * This function returns a vector of all the objects momentanly avaiable in the database                                                                                                                     
	 * @return a vector of objects if all goes right, alternativ it returns a MyError                                                                                                                              
	 */
	public static  Linea  getByNumLin(String linecode, String table_prefix)
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		String[] args = {linecode};
		Cursor cursor = sqlite.rawQuery("select * from " + table_prefix + "linee where num_lin = ?", args);
		Linea line = null;
		if(cursor.moveToFirst())
		{
			line = new Linea(cursor);
		}
		cursor.close();
		sqlite.close();
		return line;
	}
}
