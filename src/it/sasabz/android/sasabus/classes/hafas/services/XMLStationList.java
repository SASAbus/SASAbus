/**
 *
 * XMLStationList.java
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
package it.sasabz.android.sasabus.classes.hafas.services;

import it.sasabz.android.sasabus.classes.hafas.XMLRequest;
import it.sasabz.android.sasabus.classes.hafas.XMLStation;
import it.sasabz.android.sasabus.classes.network.SASAbusXML;
import it.sasabz.android.sasabus.fragments.OnlineSelectFragment;

import java.util.Vector;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.util.Log;

public class XMLStationList extends AsyncTask<String, Void, Integer> {
	
	private OnlineSelectFragment activity = null;
	
	private Vector<XMLStation> from = null;
	private Vector<XMLStation> to = null;

	public XMLStationList(OnlineSelectFragment activity)
	{
		this.activity = activity;
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		try
		{
			for(int u = 0; u < params.length; ++ u)
			{
				String xml = XMLRequest.locValRequest(params[u]);
				if(xml == "" || XMLRequest.containsError(xml))
				{
					Log.e("XML-ERROR", xml);
					return Integer.valueOf(Integer.MAX_VALUE);
				}
				SASAbusXML parser = new SASAbusXML();
				
				Document doc = parser.getDomElement(xml);
				
				NodeList nl = doc.getElementsByTagName("Station");
				
				Vector<XMLStation> list = new Vector<XMLStation>();
				
				for(int i = 0; i < nl.getLength(); ++i)
				{
					Node node = nl.item(i);
					if(node.hasAttributes())
					{
						if(list == null)
						{
							list = new Vector<XMLStation>();
						}
						XMLStation station = new XMLStation();
						NamedNodeMap attributelist = node.getAttributes();
						for(int j = 0; j < attributelist.getLength(); ++ j)
						{
							station.setProperty(attributelist.item(j).getNodeName(), attributelist.item(j).getNodeValue());
						}
						list.add(station);
					}
				}
				if(u == 0)
				{
					this.from = list;
				}
				else
				{
					this.to = list;
				}
			}
		}
		catch(Exception e)
		{
			Log.v("XML-STATION-LIST", "ERROR", e);
		}
		return Integer.valueOf(params.length);
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if(Integer.MAX_VALUE == result)
		{
			activity.myShowDialog(OnlineSelectFragment.XML_FAILURE);
			return;
		}
		activity.fillSpinner(from, to);
	}
}
