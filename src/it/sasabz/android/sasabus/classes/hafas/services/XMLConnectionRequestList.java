/**
 *
 * XMLConnectionRequestList.java
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

import it.sasabz.android.sasabus.classes.hafas.XMLAttributVariante;
import it.sasabz.android.sasabus.classes.hafas.XMLBasicStop;
import it.sasabz.android.sasabus.classes.hafas.XMLConnection;
import it.sasabz.android.sasabus.classes.hafas.XMLConnectionRequest;
import it.sasabz.android.sasabus.classes.hafas.XMLJourney;
import it.sasabz.android.sasabus.classes.hafas.XMLRequest;
import it.sasabz.android.sasabus.classes.hafas.XMLStation;
import it.sasabz.android.sasabus.classes.hafas.XMLWalk;
import it.sasabz.android.sasabus.classes.network.SASAbusXML;
import it.sasabz.android.sasabus.fragments.OnlineShowFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.util.Log;

public class XMLConnectionRequestList extends AsyncTask<Void, Void, Vector<XMLConnectionRequest>>{
	
	private XMLStation from;
	private XMLStation to;
	private Date datetime;
	protected OnlineShowFragment activity;
	
	public XMLConnectionRequestList()
	{
		
	}
	
	public XMLConnectionRequestList(XMLStation from, XMLStation to, Date datetime, OnlineShowFragment activity)
	{
		this.from = from;
		this.to = to;
		this.datetime = datetime;
		this.activity = activity;
	}
	
	protected XMLConnectionRequest scrollForward(XMLConnectionRequest request)
	{
		XMLConnectionRequest req = new XMLConnectionRequest();
		String xml = XMLRequest.conScrollRequest(request.getContext(), true);
		if(xml == "" || XMLRequest.containsError(xml))
		{
			Log.e("XML-ERROR", xml);
			return null;
		}
		SASAbusXML parser = new SASAbusXML();
		
		Document doc = parser.getDomElement(xml);
		
		NodeList nl = doc.getElementsByTagName("ConResCtxt");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			Node node = nl.item(0).getChildNodes().item(0);	
			req.setContext(node.getNodeValue());
		}
		
		
		nl = doc.getElementsByTagName("Overview");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			NodeList overview = nl.item(0).getChildNodes();
			for(int i = 0; i < overview.getLength(); ++ i)
			{
				Node node = overview.item(i);
				if(node.getNodeName().equals("Departure"))
				{
					XMLBasicStop departurestop = new XMLBasicStop();
					NodeList departure = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < departure.getLength(); ++ j)
					{
						Node depnode = departure.item(j);
						if(depnode.getNodeName().equals("Station"))
						{
							XMLStation depstat = new XMLStation();
							NamedNodeMap attributelist = depnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								depstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							departurestop.setStation(depstat);
						}
						else if(depnode.getNodeName().equals("Dep"))
						{
							NodeList dep = depnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node depnode2 = dep.item(k);
								if(depnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setDeparture(departurestop);
					}
				}
				else if (node.getNodeName().equals("Arrival"))
				{
					XMLBasicStop arrivalstop = new XMLBasicStop();
					NodeList arrival = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < arrival.getLength(); ++ j)
					{
						Node arrnode = arrival.item(j);
						if(arrnode.getNodeName().equals("Station"))
						{
							XMLStation arrstat = new XMLStation();
							NamedNodeMap attributelist = arrnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								arrstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							arrivalstop.setStation(arrstat);
						}
						else if(arrnode.getNodeName().equals("Arr"))
						{
							NodeList dep = arrnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node arrnode2 = dep.item(k);
								if(arrnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setArrival(arrivalstop);
					}
				}
				else if (node.getNodeName().equals("Transfers"))
				{
					req.setTransfers(Integer.parseInt(node.getChildNodes().item(0).getNodeValue()));
				}
				else if(node.getNodeName().equals("Duration"))
				{
					SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
					try
					{
						req.setDuration(simple.parse((node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
					}
					catch(Exception e)
					{
						Log.e("XML-LOGGER", "Datumskonvertierung falsch", e);
					}
				}
			}
			nl = doc.getElementsByTagName("ConSectionList");
			
			if(nl.item(0) != null && nl.getLength() >= 1)
			{
				NodeList conlist = nl.item(0).getChildNodes();
				Vector<XMLConnection> convect = new Vector<XMLConnection>();
				for(int i = 0; i < conlist.getLength(); ++ i)
				{
					Node conlistitem = conlist.item(i);
					if(conlistitem.getNodeName().equals("ConSection"))
					{
						XMLBasicStop departurestop = null;
						XMLBasicStop arrivalstop = null;
						XMLConnection con = null;
						NodeList consection = conlistitem.getChildNodes();
						for(int k = 0; k < consection.getLength(); ++ k)
						{
							Node node = consection.item(k);
							if(node.getNodeName().equals("Departure"))
							{
								departurestop = new XMLBasicStop();
								NodeList departure = node.getChildNodes().item(0).getChildNodes();
								for(int j = 0; j < departure.getLength(); ++ j)
								{
									Node depnode = departure.item(j);
									if(depnode.getNodeName().equals("Station"))
									{
										XMLStation depstat = new XMLStation();
										NamedNodeMap attributelist = depnode.getAttributes();
										for(int n = 0; n < attributelist.getLength(); ++n)
										{
											depstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
										}
										departurestop.setStation(depstat);
									}
									else if(depnode.getNodeName().equals("Dep"))
									{
										NodeList dep = depnode.getChildNodes();
										for(int n = 0; n < dep.getLength(); ++ n)
										{
											Node depnode2 = dep.item(n);
											if(depnode2.getNodeName().equals("Time"))
											{
												SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
												try
												{
													departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
												}
												catch(Exception e)
												{
													Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
												}
											}
										}
									}
								}
							}
							else if (node.getNodeName().equals("Arrival"))
							{
								arrivalstop = new XMLBasicStop();
								NodeList arrival = node.getChildNodes().item(0).getChildNodes();
								for(int j = 0; j < arrival.getLength(); ++ j)
								{
									Node arrnode = arrival.item(j);
									if(arrnode.getNodeName().equals("Station"))
									{
										XMLStation arrstat = new XMLStation();
										NamedNodeMap attributelist = arrnode.getAttributes();
										for(int n = 0; n < attributelist.getLength(); ++n)
										{
											arrstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
										}
										arrivalstop.setStation(arrstat);
									}
									else if(arrnode.getNodeName().equals("Arr"))
									{
										NodeList dep = arrnode.getChildNodes();
										for(int n = 0; n < dep.getLength(); ++ n)
										{
											Node arrnode2 = dep.item(n);
											if(arrnode2.getNodeName().equals("Time"))
											{
												SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
												try
												{
													arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
												}
												catch(Exception e)
												{
													Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
												}
											}
										}
									}
									req.setArrival(arrivalstop);
								}
							}
							else if(node.getNodeName().equals("Journey"))
							{
								con = new XMLJourney();
								NodeList journey = node.getChildNodes();
								for(int s = 0; s < journey.getLength(); ++ s)
								{
									Node journeyitem = journey.item(s);
									if(journeyitem.getNodeName().equals("JourneyAttributeList"))
									{
										Vector<XMLAttributVariante> attrlist = new Vector<XMLAttributVariante>();
										NodeList journeyattrlist = journeyitem.getChildNodes();
										for(int g = 0; g < journeyattrlist.getLength(); ++ g)
										{
											XMLAttributVariante variante = new XMLAttributVariante();
											Node attr = journeyattrlist.item(g);
											NamedNodeMap attribute = attr.getChildNodes().item(0).getAttributes();
											if(attribute != null && attribute.getNamedItem("type") != null)
											{
												variante.setType(attribute.getNamedItem("type").getNodeValue());
												variante.setText(attr.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
												attrlist.add(variante);
											}
										}
										((XMLJourney)con).setAttributlist(attrlist);
									}
								}
							}
							else if (node.getNodeName().equals("Walk"))
							{
								con = new XMLWalk();
								NodeList walklist = node.getChildNodes();
								for(int m = 0; m < walklist.getLength(); ++ m)
								{
									Node walklistitem = walklist.item(m);
									if(walklistitem.getNodeName().equals("Duration"))
									{
										SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
										try
										{
											con.setDuration(simple.parse((walklistitem.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
										}
										catch(Exception e)
										{
											Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
										}
									}
								}
							}
						}
						if(con == null)
						{
							Log.v("XML-LOGGER", "No connection type detected!");
							System.exit(-3);
						}
						con.setDeparture(departurestop);
						con.setArrival(arrivalstop);
						if(con instanceof XMLJourney)
						{
							Date duration = new Date((arrivalstop.getArrtime().getTime() - departurestop.getArrtime().getTime()) - 3600000);
							con.setDuration(duration);
						}
						convect.add(con);
					}
					req.setConnectionlist(convect);
				}
			}
			
		}
		return req;
	}
	
	protected XMLConnectionRequest scrollBackward(XMLConnectionRequest request)
	{
		XMLConnectionRequest req = new XMLConnectionRequest();
		String xml = XMLRequest.conScrollRequest(request.getContext(), false);
		if(xml == "" || XMLRequest.containsError(xml))
		{
			Log.e("XML-ERROR", xml);
			return null;
		}
		SASAbusXML parser = new SASAbusXML();
		
		Document doc = parser.getDomElement(xml);
		
		NodeList nl = doc.getElementsByTagName("ConResCtxt");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			Node node = nl.item(0).getChildNodes().item(0);	
			req.setContext(node.getNodeValue());
		}
		
		
		nl = doc.getElementsByTagName("Overview");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			NodeList overview = nl.item(0).getChildNodes();
			for(int i = 0; i < overview.getLength(); ++ i)
			{
				Node node = overview.item(i);
				if(node.getNodeName().equals("Departure"))
				{
					XMLBasicStop departurestop = new XMLBasicStop();
					NodeList departure = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < departure.getLength(); ++ j)
					{
						Node depnode = departure.item(j);
						if(depnode.getNodeName().equals("Station"))
						{
							XMLStation depstat = new XMLStation();
							NamedNodeMap attributelist = depnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								depstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							departurestop.setStation(depstat);
						}
						else if(depnode.getNodeName().equals("Dep"))
						{
							NodeList dep = depnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node depnode2 = dep.item(k);
								if(depnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setDeparture(departurestop);
					}
				}
				else if (node.getNodeName().equals("Arrival"))
				{
					XMLBasicStop arrivalstop = new XMLBasicStop();
					NodeList arrival = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < arrival.getLength(); ++ j)
					{
						Node arrnode = arrival.item(j);
						if(arrnode.getNodeName().equals("Station"))
						{
							XMLStation arrstat = new XMLStation();
							NamedNodeMap attributelist = arrnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								arrstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							arrivalstop.setStation(arrstat);
						}
						else if(arrnode.getNodeName().equals("Arr"))
						{
							NodeList dep = arrnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node arrnode2 = dep.item(k);
								if(arrnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setArrival(arrivalstop);
					}
				}
				else if (node.getNodeName().equals("Transfers"))
				{
					req.setTransfers(Integer.parseInt(node.getChildNodes().item(0).getNodeValue()));
				}
				else if(node.getNodeName().equals("Duration"))
				{
					SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
					try
					{
						req.setDuration(simple.parse((node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
					}
					catch(Exception e)
					{
						Log.e("XML-LOGGER", "Datumskonvertierung falsch", e);
					}
				}
			}
			nl = doc.getElementsByTagName("ConSectionList");
			
			if(nl.item(0) != null && nl.getLength() >= 1)
			{
				NodeList conlist = nl.item(0).getChildNodes();
				Vector<XMLConnection> convect = new Vector<XMLConnection>();
				for(int i = 0; i < conlist.getLength(); ++ i)
				{
					Node conlistitem = conlist.item(i);
					if(conlistitem.getNodeName().equals("ConSection"))
					{
						XMLBasicStop departurestop = null;
						XMLBasicStop arrivalstop = null;
						XMLConnection con = null;
						NodeList consection = conlistitem.getChildNodes();
						for(int k = 0; k < consection.getLength(); ++ k)
						{
							Node node = consection.item(k);
							if(node.getNodeName().equals("Departure"))
							{
								departurestop = new XMLBasicStop();
								NodeList departure = node.getChildNodes().item(0).getChildNodes();
								for(int j = 0; j < departure.getLength(); ++ j)
								{
									Node depnode = departure.item(j);
									if(depnode.getNodeName().equals("Station"))
									{
										XMLStation depstat = new XMLStation();
										NamedNodeMap attributelist = depnode.getAttributes();
										for(int n = 0; n < attributelist.getLength(); ++n)
										{
											depstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
										}
										departurestop.setStation(depstat);
									}
									else if(depnode.getNodeName().equals("Dep"))
									{
										NodeList dep = depnode.getChildNodes();
										for(int n = 0; n < dep.getLength(); ++ n)
										{
											Node depnode2 = dep.item(n);
											if(depnode2.getNodeName().equals("Time"))
											{
												SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
												try
												{
													departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
												}
												catch(Exception e)
												{
													Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
												}
											}
										}
									}
								}
							}
							else if (node.getNodeName().equals("Arrival"))
							{
								arrivalstop = new XMLBasicStop();
								NodeList arrival = node.getChildNodes().item(0).getChildNodes();
								for(int j = 0; j < arrival.getLength(); ++ j)
								{
									Node arrnode = arrival.item(j);
									if(arrnode.getNodeName().equals("Station"))
									{
										XMLStation arrstat = new XMLStation();
										NamedNodeMap attributelist = arrnode.getAttributes();
										for(int n = 0; n < attributelist.getLength(); ++n)
										{
											arrstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
										}
										arrivalstop.setStation(arrstat);
									}
									else if(arrnode.getNodeName().equals("Arr"))
									{
										NodeList dep = arrnode.getChildNodes();
										for(int n = 0; n < dep.getLength(); ++ n)
										{
											Node arrnode2 = dep.item(n);
											if(arrnode2.getNodeName().equals("Time"))
											{
												SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
												try
												{
													arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
												}
												catch(Exception e)
												{
													Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
												}
											}
										}
									}
									req.setArrival(arrivalstop);
								}
							}
							else if(node.getNodeName().equals("Journey"))
							{
								con = new XMLJourney();
								NodeList journey = node.getChildNodes();
								for(int s = 0; s < journey.getLength(); ++ s)
								{
									Node journeyitem = journey.item(s);
									if(journeyitem.getNodeName().equals("JourneyAttributeList"))
									{
										Vector<XMLAttributVariante> attrlist = new Vector<XMLAttributVariante>();
										NodeList journeyattrlist = journeyitem.getChildNodes();
										for(int g = 0; g < journeyattrlist.getLength(); ++ g)
										{
											XMLAttributVariante variante = new XMLAttributVariante();
											Node attr = journeyattrlist.item(g);
											NamedNodeMap attribute = attr.getChildNodes().item(0).getAttributes();
											if(attribute != null && attribute.getNamedItem("type") != null)
											{
												variante.setType(attribute.getNamedItem("type").getNodeValue());
												variante.setText(attr.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
												attrlist.add(variante);
											}
										}
										((XMLJourney)con).setAttributlist(attrlist);
									}
								}
							}
							else if (node.getNodeName().equals("Walk"))
							{
								con = new XMLWalk();
								NodeList walklist = node.getChildNodes();
								for(int m = 0; m < walklist.getLength(); ++ m)
								{
									Node walklistitem = walklist.item(m);
									if(walklistitem.getNodeName().equals("Duration"))
									{
										SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
										try
										{
											con.setDuration(simple.parse((walklistitem.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
										}
										catch(Exception e)
										{
											Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
										}
									}
								}
							}
						}
						if(con == null)
						{
							Log.v("XML-LOGGER", "No connection type detected!");
							System.exit(-3);
						}
						con.setDeparture(departurestop);
						con.setArrival(arrivalstop);
						if(con instanceof XMLJourney)
						{
							Date duration = new Date((arrivalstop.getArrtime().getTime() - departurestop.getArrtime().getTime()) - 3600000);
							con.setDuration(duration);
						}
						convect.add(con);
					}
					req.setConnectionlist(convect);
				}
			}
		}
		return req;
	}

	private XMLConnectionRequest getRequest()
	{
		XMLConnectionRequest req = new XMLConnectionRequest();
		String xml = XMLRequest.conRequest(from, to, datetime);
		if(xml == "" || XMLRequest.containsError(xml))
		{
			Log.e("XML-ERROR", xml);
			return null;
		}
		SASAbusXML parser = new SASAbusXML();
		
		Document doc = parser.getDomElement(xml);
		
		NodeList nl = doc.getElementsByTagName("ConResCtxt");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			Node node = nl.item(0).getChildNodes().item(0);	
			req.setContext(node.getNodeValue());
		}
		
		
		nl = doc.getElementsByTagName("Overview");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			NodeList overview = nl.item(0).getChildNodes();
			for(int i = 0; i < overview.getLength(); ++ i)
			{
				Node node = overview.item(i);
				if(node.getNodeName().equals("Departure"))
				{
					XMLBasicStop departurestop = new XMLBasicStop();
					NodeList departure = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < departure.getLength(); ++ j)
					{
						Node depnode = departure.item(j);
						if(depnode.getNodeName().equals("Station"))
						{
							XMLStation depstat = new XMLStation();
							NamedNodeMap attributelist = depnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								depstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							departurestop.setStation(depstat);
						}
						else if(depnode.getNodeName().equals("Dep"))
						{
							NodeList dep = depnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node depnode2 = dep.item(k);
								if(depnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setDeparture(departurestop);
					}
				}
				else if (node.getNodeName().equals("Arrival"))
				{
					XMLBasicStop arrivalstop = new XMLBasicStop();
					NodeList arrival = node.getChildNodes().item(0).getChildNodes();
					for(int j = 0; j < arrival.getLength(); ++ j)
					{
						Node arrnode = arrival.item(j);
						if(arrnode.getNodeName().equals("Station"))
						{
							XMLStation arrstat = new XMLStation();
							NamedNodeMap attributelist = arrnode.getAttributes();
							for(int k = 0; k < attributelist.getLength(); ++k)
							{
								arrstat.setProperty(attributelist.item(k).getNodeName(), attributelist.item(k).getNodeValue());
							}
							arrivalstop.setStation(arrstat);
						}
						else if(arrnode.getNodeName().equals("Arr"))
						{
							NodeList dep = arrnode.getChildNodes();
							for(int k = 0; k < dep.getLength(); ++ k)
							{
								Node arrnode2 = dep.item(k);
								if(arrnode2.getNodeName().equals("Time"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
						req.setArrival(arrivalstop);
					}
				}
				else if (node.getNodeName().equals("Transfers"))
				{
					req.setTransfers(Integer.parseInt(node.getChildNodes().item(0).getNodeValue()));
				}
				else if(node.getNodeName().equals("Duration"))
				{
					SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
					try
					{
						req.setDuration(simple.parse((node.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
					}
					catch(Exception e)
					{
						Log.e("XML-LOGGER", "Datumskonvertierung falsch", e);
					}
				}
			}
			
		}
		
		nl = doc.getElementsByTagName("ConSectionList");
		
		if(nl.item(0) != null && nl.getLength() >= 1)
		{
			NodeList conlist = nl.item(0).getChildNodes();
			Vector<XMLConnection> convect = new Vector<XMLConnection>();
			for(int i = 0; i < conlist.getLength(); ++ i)
			{
				Node conlistitem = conlist.item(i);
				if(conlistitem.getNodeName().equals("ConSection"))
				{
					XMLBasicStop departurestop = null;
					XMLBasicStop arrivalstop = null;
					XMLConnection con = null;
					NodeList consection = conlistitem.getChildNodes();
					for(int k = 0; k < consection.getLength(); ++ k)
					{
						Node node = consection.item(k);
						if(node.getNodeName().equals("Departure"))
						{
							departurestop = new XMLBasicStop();
							NodeList departure = node.getChildNodes().item(0).getChildNodes();
							for(int j = 0; j < departure.getLength(); ++ j)
							{
								Node depnode = departure.item(j);
								if(depnode.getNodeName().equals("Station"))
								{
									XMLStation depstat = new XMLStation();
									NamedNodeMap attributelist = depnode.getAttributes();
									for(int n = 0; n < attributelist.getLength(); ++n)
									{
										depstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
									}
									departurestop.setStation(depstat);
								}
								else if(depnode.getNodeName().equals("Dep"))
								{
									NodeList dep = depnode.getChildNodes();
									for(int n = 0; n < dep.getLength(); ++ n)
									{
										Node depnode2 = dep.item(n);
										if(depnode2.getNodeName().equals("Time"))
										{
											SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
											try
											{
												departurestop.setArrtime(simple.parse((depnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
											}
											catch(Exception e)
											{
												Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
											}
										}
									}
								}
							}
						}
						else if (node.getNodeName().equals("Arrival"))
						{
							arrivalstop = new XMLBasicStop();
							NodeList arrival = node.getChildNodes().item(0).getChildNodes();
							for(int j = 0; j < arrival.getLength(); ++ j)
							{
								Node arrnode = arrival.item(j);
								if(arrnode.getNodeName().equals("Station"))
								{
									XMLStation arrstat = new XMLStation();
									NamedNodeMap attributelist = arrnode.getAttributes();
									for(int n = 0; n < attributelist.getLength(); ++n)
									{
										arrstat.setProperty(attributelist.item(n).getNodeName(), attributelist.item(n).getNodeValue());
									}
									arrivalstop.setStation(arrstat);
								}
								else if(arrnode.getNodeName().equals("Arr"))
								{
									NodeList dep = arrnode.getChildNodes();
									for(int n = 0; n < dep.getLength(); ++ n)
									{
										Node arrnode2 = dep.item(n);
										if(arrnode2.getNodeName().equals("Time"))
										{
											SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
											try
											{
												arrivalstop.setArrtime(simple.parse((arrnode2.getChildNodes().item(0).getNodeValue()).substring(3)));
											}
											catch(Exception e)
											{
												Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
											}
										}
									}
								}
								req.setArrival(arrivalstop);
							}
						}
						else if(node.getNodeName().equals("Journey"))
						{
							con = new XMLJourney();
							NodeList journey = node.getChildNodes();
							for(int s = 0; s < journey.getLength(); ++ s)
							{
								Node journeyitem = journey.item(s);
								if(journeyitem.getNodeName().equals("JourneyAttributeList"))
								{
									Vector<XMLAttributVariante> attrlist = new Vector<XMLAttributVariante>();
									NodeList journeyattrlist = journeyitem.getChildNodes();
									for(int g = 0; g < journeyattrlist.getLength(); ++ g)
									{
										XMLAttributVariante variante = new XMLAttributVariante();
										Node attr = journeyattrlist.item(g);
										NamedNodeMap attribute = attr.getChildNodes().item(0).getAttributes();
										if(attribute != null && attribute.getNamedItem("type") != null)
										{
											variante.setType(attribute.getNamedItem("type").getNodeValue());
											variante.setText(attr.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(0).getNodeValue());
											attrlist.add(variante);
										}
									}
									((XMLJourney)con).setAttributlist(attrlist);
								}
							}
						}
						else if (node.getNodeName().equals("Walk"))
						{
							con = new XMLWalk();
							NodeList walklist = node.getChildNodes();
							for(int m = 0; m < walklist.getLength(); ++ m)
							{
								Node walklistitem = walklist.item(m);
								if(walklistitem.getNodeName().equals("Duration"))
								{
									SimpleDateFormat simple = new SimpleDateFormat("HH:mm:ss");
									try
									{
										con.setDuration(simple.parse((walklistitem.getChildNodes().item(0).getChildNodes().item(0).getNodeValue()).substring(3)));
									}
									catch(Exception e)
									{
										Log.e("XML-LOGGER", "Datumkonvertierungsfehler", e);
									}
								}
							}
						}
					}
					if(con == null)
					{
						Log.v("XML-LOGGER", "No connection type detected!");
						return null;
					}
					con.setDeparture(departurestop);
					con.setArrival(arrivalstop);
					if(con instanceof XMLJourney)
					{
						Date duration = new Date((arrivalstop.getArrtime().getTime() - departurestop.getArrtime().getTime()) - 3600000);
						con.setDuration(duration);
					}
					convect.add(con);
				}
				req.setConnectionlist(convect);
			}
		}
		return req;
	}
	
	@Override
	protected Vector<XMLConnectionRequest> doInBackground(Void... params) {
		Vector<XMLConnectionRequest> ret = null; 
		XMLConnectionRequest req = getRequest();
		if(req != null)
		{
			ret = new Vector<XMLConnectionRequest>();
			ret.add(req);
			XMLConnectionRequest back = scrollBackward(req);
			if(back != null && !req.getDeparture().getArrtime().equals(back.getDeparture().getArrtime()) && 
					!req.getArrival().getArrtime().equals(back.getArrival().getArrtime()))
			{
				ret.add(0, back);
			}
			XMLConnectionRequest forward = scrollForward(req);
			if(forward != null && !req.getDeparture().getArrtime().equals(forward.getDeparture().getArrtime()) && 
					!req.getArrival().getArrtime().equals(forward.getArrival().getArrtime()))
			{
				ret.add(forward);
			}
		}
		return ret;
	}
	
	
	@Override
	protected void onPostExecute(Vector <XMLConnectionRequest> result) {
		super.onPostExecute(result);
		if(result == null)
		{
			activity.myShowDialog(OnlineShowFragment.NO_DATA);
			return;
		}
		activity.fillData(result);
	}
}
