/**
 *
 * BacinoList.java
 * 
 * Created: Dez 13, 2011 16:20:40 PM
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

import it.sasabz.android.sasabus.SASAbus;
import it.sasabz.android.sasabus.classes.adapter.MySQLiteDBAdapter;

import java.util.Iterator;
import java.util.Vector;

import android.database.Cursor;

public class BacinoList {
	
	 
	/**                                                                                                                                                                                                          
	 * This function returns a vector of all the objects momentanly avaiable in the database                                                                                                                     
	 * @return a vector of objects if all goes right, alternativ it returns a MyError                                                                                                                              
	 */
	public static  Vector <DBObject>  getList()
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		Cursor cursor = sqlite.rawQuery("select * from  bacini", null);
		Vector <DBObject> list = null;
		if(cursor.moveToFirst())
		{
			int i = 0;
			list = new Vector<DBObject>();
			do {
				Bacino element = new Bacino(cursor);
				list.add(i, element);
				++i;
			} while(cursor.moveToNext());
		}
		cursor.close();
		sqlite.close();
		return list;
	}
	
	public static Bacino getBacino(String start, String stop, String linecode)
	{
		Bacino ret = null;
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		Vector<DBObject> list = getList();
		Iterator<DBObject> iter = list.iterator();
		boolean gefunden = false;
		while(iter.hasNext() && !gefunden)
		{
			Bacino bac = (Bacino)iter.next();
			String[] args = {linecode, start, stop};
			Cursor cursor = sqlite.rawQuery("select * " +
    				"from "+
    				"(select id, lineaId " +
    				"from " + bac.getTable_prefix() + "corse as corse "+
    				"where " +
    				"lineaId = (" +
    				"Select id from " + bac.getTable_prefix() + "linee where num_lin = ?) ) as c, " +
    				"(select progressivo, orario, corsaId "+
    				"from " + bac.getTable_prefix() + "orarii "+
    				"where palinaId IN (" +
    				"select id from paline where nome_de = ?" +
    				")) as o1, " +
    				"(select progressivo , corsaId "+
    				"from " + bac.getTable_prefix() + "orarii " +
    				"where palinaId IN (" +
    				"select id from paline where nome_de = ?" +
    				")) as o2 " +
    				"where o1.progressivo < o2.progressivo " +
    				"and c.id = o1.corsaId " +
    				"and c.id = o2.corsaId " +
    				"LIMIT 1 ", 
    				args);
			if(cursor.moveToFirst())
			{
				ret = bac;
				gefunden = true;
			}
			cursor.close();

		}
		sqlite.close();
		return ret;
	}
	
	public static  Bacino  getById(int id)
	{
		MySQLiteDBAdapter sqlite = MySQLiteDBAdapter.getInstance(SASAbus.getContext());
		String args[] = {Integer.toString(id)};
		Cursor cursor = sqlite.rawQuery("select * from  bacini where id = ?", args);
		Bacino bacino = null;
		if(cursor.moveToFirst())
		{
			bacino = new Bacino(cursor);
		}
		cursor.close();
		sqlite.close();
		return bacino;
	}

}
