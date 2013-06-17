/**
 *
 * XMLStation.java
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
package it.sasabz.android.sasabus.classes.hafas;

import it.sasabz.android.sasabus.classes.dbobjects.Palina;
import it.sasabz.android.sasabus.classes.dbobjects.PalinaList;
import it.sasabz.android.sasabus.classes.network.SASAbusXML;

import java.util.Locale;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLStation {
	
	private String name = null;
	
	private String externalId = "";
	
	private int externalStationNr = 0;
	
	private String type = "";
	
	private int xE6 = 0;
	
	private int yE6 = 0;
	
	
	public XMLStation()
	{
		
	}
	
	public XMLStation(String name, String externalId, int externalStationNr, String type, int xE6, int yE6)
	{
		this.name = name;
		this.externalId = externalId;
		this.externalStationNr = externalStationNr;
		this.type = type;
		this.xE6 = xE6;
		this.yE6 = yE6;
	}
	
	public void setProperty(String name, String value)
	{
		if(name.equals("name"))
		{
			this.name = value;
		}
		else if (name.equals("externalId"))
		{
			this.externalId = value;
		}
		else if(name.equals("externalStationNr"))
		{
			this.externalStationNr = Integer.parseInt(value);
		}
		else if(name.equals("type"))
		{
			this.type = value;
		}
		else if(name.equals("y"))
		{
			this.yE6 = Integer.parseInt(value);
		}
		else if (name.equals("x"))
		{
			this.xE6 = Integer.parseInt(value);
		}
	}
	
	public String getHaltestelle()
	{
		String ret = "";
		if((Locale.getDefault().getLanguage()).indexOf(Locale.GERMAN.toString()) != -1)
		{
			String italienisch = this.name.substring(0, this.name.indexOf("-")).trim();
			Palina station = PalinaList.getTranslation(italienisch, "it");
			if(station != null)
			{
				ret = station.getName_de();
				ret =  ret.substring(1, ret.indexOf(")"))+ " - " + ret.substring(ret.indexOf(")") + 1).trim();
			}
			else
			{
				String geteilt = this.name.substring(this.name.indexOf("-") + 1).trim();
				ret =  geteilt.substring(1, geteilt.indexOf(")"))+ " - " +geteilt.substring(geteilt.indexOf(")") + 1).trim();
			}
		}
		else
		{
			String geteilt = this.name.substring(0, this.name.indexOf("-")).trim();
			ret = geteilt.substring(1, geteilt.indexOf(")")) + " - " + geteilt.substring(geteilt.indexOf(")") + 1).trim();
		}
		return ret;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}



	/**
	 * @return the externalId
	 */
	public String getExternalId() {
		return externalId;
	}



	/**
	 * @return the externalStationNr
	 */
	public int getExternalStationNr() {
		return externalStationNr;
	}



	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}



	/**
	 * @return the xE6
	 */
	public int getxE6() {
		return xE6;
	}



	/**
	 * @return the yE6
	 */
	public int getyE6() {
		return yE6;
	}


	@Override
	public String toString()
	{
		return this.name;
	}

	public void fromXMLString(String xml)
	{
		SASAbusXML parser = new SASAbusXML();
		
		Document doc = parser.getDomElement(xml);
		
		NodeList nl = doc.getElementsByTagName("Station");
		
		Node node = nl.item(0);
		if(node != null && node.hasAttributes())
		{
			NamedNodeMap attributelist = node.getAttributes();
			for(int j = 0; j < attributelist.getLength(); ++ j)
			{
				setProperty(attributelist.item(j).getNodeName(), attributelist.item(j).getNodeValue());
			}
		}
	}
	
	public String toXMLString()
	{
		return "<Station name=\"" + this.name + "\" externalId=\"" + this.externalId + 
				"\" externalStationNr=\"" + this.externalStationNr + "\" type=\"" + this.type + 
				"\" x=\"" + this.xE6 + "\" y=\"" + this.yE6 + "\" />";
	}
}
