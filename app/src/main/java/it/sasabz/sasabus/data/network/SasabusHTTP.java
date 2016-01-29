/*
 * SASAbus - Android app for SASA bus open data
 *
 * SasabusHTTP.java
 *
 * Created: May 1, 2012 18:20:40 PM
 *
 * Copyright (C) 2011-2014 Paolo Dongilli, Markus Windegger, Davide Montesin
 *
 * This file is part of SASAbus.
 *
 * SASAbus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SASAbus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SASAbus.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.sasabus.data.network;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class SasabusHTTP
{

   private String hostname;

   public SasabusHTTP(String hostname)
   {
      this.hostname = hostname;
   }

   public synchronized Date getModificationTime(String filename) throws IOException
   {
      URL url = new URL(this.hostname + filename);
      URLConnection ucon = url.openConnection();

      ucon.connect();

      long date_ms = ucon.getLastModified();

      Date date = new Date(date_ms);
      Log.v("SASAbus HTTP", "Datum last mod: " + date.toLocaleString());
      return date;
   }

   /**
    * Requesting data via post-request using the apache http-classes
    * 
    * @throws IOException
    * @throws ClientProtocolException
    */
   public String postData() throws ClientProtocolException, IOException
   {
      HttpClient httpclient = new DefaultHttpClient();

      HttpPost post = new HttpPost(hostname);

      Log.v("HOSTNAME", hostname);

      HttpResponse rp = httpclient.execute(post);

      String responseBody = null;

      if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
         responseBody = EntityUtils.toString(rp.getEntity());
      }
      return responseBody;
   }

   /**
    * Requesting data via post-request using the apache http-classes
    * 
    * @throws IOException
    * @throws ClientProtocolException
    */
   public String postData(List<NameValuePair> params) throws ClientProtocolException, IOException
   {
      HttpClient httpclient = new DefaultHttpClient();

      HttpPost post = new HttpPost(hostname);

      Log.v("HOSTNAME", hostname);

      post.setEntity(new UrlEncodedFormEntity(params));

      HttpResponse rp = httpclient.execute(post);

      String responseBody = null;

      if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
      {
         responseBody = EntityUtils.toString(rp.getEntity());
      }
      return responseBody;
   }

}