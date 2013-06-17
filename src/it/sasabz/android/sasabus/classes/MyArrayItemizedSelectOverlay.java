/**
 *
 * MyArrayItemizedOverlay.java
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

import java.util.Iterator;
import java.util.Vector;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MyArrayItemizedSelectOverlay extends ArrayItemizedOverlay {

	private Vector<MyOverlaySelectItem> overlays = new Vector<MyOverlaySelectItem>();
	
	public MyArrayItemizedSelectOverlay(Drawable defaultMarker) {
		super(defaultMarker);
	}
	
	public void addItem(MyOverlaySelectItem item)
	{
		super.addItem(item);
		overlays.add(item);
	}
	
	@Override
	public boolean onTap(GeoPoint point, MapView view)
	{
		Iterator<MyOverlaySelectItem> iter = overlays.iterator();
		while(iter.hasNext())
		{
			MyOverlaySelectItem item = iter.next();
			Projection projection = view.getProjection();
			
			Rect rect_draw = item.getMarker().getBounds();
			
			Point item_point  = projection.toPixels(item.getPoint(), null);
			Point click_point = projection.toPixels(point, null);
			
			Rect probe_rect = new Rect(rect_draw.left + item_point.x, rect_draw.top + item_point.y, 
					rect_draw.right + item_point.x, rect_draw.bottom + item_point.y);
			
			if(probe_rect.contains(click_point.x, click_point.y))
			{
				item.onTap(view.getContext());
			}
			
		}
		return false;
	}
	

}
