/*
 * SASAbus - Android app for SASA bus open data
 *
 * BusDepartureItem.java
 *
 * Created: Jan 27, 2014 10:55:00 AM
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

package it.sasabz.sasabus.ui.busschedules;

import android.util.Log;

import java.io.Serializable;

import it.sasabz.sasabus.opendata.client.model.BusTripBusStopTime;

public class BusDepartureItem implements Serializable
{
   private String       time;
   private String       busStopOrLineName;
   private String       destinationName;

   MyBusTripBusStopTime[] stopTimes;
   int                  selected_index;
   int                  departure_index;

   boolean              isRealtime = false;

   int                  delay;
   int                  delay_index;

   public BusDepartureItem(String time,
                           String busStopOrLineName,
                           String destinationName,
                           BusTripBusStopTime[] stopTimes,
                           int selected_index,
                           int departure_index,
                           int delay,
                           int delay_index,
                           boolean isRealtime)
   {
      super();
      this.time = time;
      this.busStopOrLineName = busStopOrLineName;
      this.destinationName = destinationName;
      this.stopTimes = new MyBusTripBusStopTime[stopTimes.length];
      for(int i = 0; i < stopTimes.length; i++)
         this.stopTimes[i] = new MyBusTripBusStopTime(stopTimes[i]);
      this.selected_index = selected_index;
      this.delay = delay;
      this.delay_index = delay_index;
      this.departure_index = departure_index;
      this.isRealtime = isRealtime;
   }

   public String getTime()
   {
      return this.time;
   }

   public String getBusStopOrLineName()
   {
      return this.busStopOrLineName;
   }

   public String getDestinationName()
   {
      return this.destinationName;
   }

   public MyBusTripBusStopTime[] getStopTimes()
   {
      return this.stopTimes;
   }

   public int getSelectedIndex()
   {
      return this.selected_index;
   }

   public int getDelay_index()
   {
      return this.delay_index;
   }

   public String getDelay()
   {
      return Integer.toString(this.delay) + "'";
   }

   public int getDelayNumber()
   {
      return this.delay;
   }

   public int getDeparture_index()
   {
      return this.departure_index;
   }

   public boolean isRealtime()
   {
      return this.isRealtime;
   }

   public void setDelay(int delay)
   {
	   this.delay = delay;
   }

   public static class MyBusTripBusStopTime implements Serializable{
      int busStop;
      int seconds;

      public MyBusTripBusStopTime(BusTripBusStopTime busTripBusStopTime) {
         busStop = busTripBusStopTime.getBusStop();
         seconds = busTripBusStopTime.getSeconds();
      }

      public int getSeconds() {
         return this.seconds;
      }

      public int getBusStop() {
         return this.busStop;
      }

      public void setBusStop(int busStop) {
         this.busStop = busStop;
      }

      public void setSeconds(int seconds) {
         this.seconds = seconds;
      }

   }

}
