/**
 *
 * MyOverlayItem.java
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

import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class MyOverlayItem extends OverlayItem{
	
	public MyOverlayItem(GeoPoint point, String titel, String message,
			Drawable bus) {
		super(point, titel, message, bus);
	}

	public void onTap(Context context)
	{
		new AlertDialog.Builder(context).setTitle(this.getTitle()).setMessage(this.getSnippet()).setNeutralButton(android.R.string.ok, null).create().show();
	}
	
}
