/**
 *
 * OnlineActivity.java
 *
 * Created: Mar 15, 2012 22:40:06 PM
 *
 * Copyright (C) 2012 Paolo Dongilli and Markus Windegger
 *
 * This file is part of SasaBus.

 * SasaBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SasaBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SasaBus. If not, see <http://www.gnu.org/licenses/>.
 *
 * This activity provides a map an the possibility to show a list of
 * bus stops which were contained in a "journey" (from - to)
 *
 * This activity is responsable for all the operations allowed during online-mode.
 * It's the space for all fragments related to it.
 *
 */
package it.sasabz.sasabus.ui.routing;

import it.sasabz.sasabus.R;
import it.sasabz.sasabus.ui.dialogs.About;
import it.sasabz.sasabus.ui.dialogs.Credits;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class OnlineActivity extends FragmentActivity{
	
	
	@Override
	protected void onCreate(Bundle savedInstaceState)
	{
		super.onCreate(savedInstaceState);
		setContentView(R.layout.fragment_container);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		
		Fragment fragment;
		fragment = fragmentManager.findFragmentById(R.id.onlinefragment);
		if(fragment != null)
		{
			ft.remove(fragment);
		}
		fragment = new OnlineSearchFragment();
		ft.add(R.id.onlinefragment, fragment);
		ft.commit();
		fragmentManager.executePendingTransactions();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_route_fragment, menu);
   	 	return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
	    if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
	        {
	            this.finish();
	            return true;
	        }
	        else
	        {
	            getSupportFragmentManager().popBackStack();
	            removeCurrentFragment();
	            return true;
	        }
	    }
	    return super.onKeyDown(keyCode, event);
	}


	public void removeCurrentFragment()
	{
	    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
	    Fragment currentFrag =  getSupportFragmentManager().findFragmentById(R.id.onlinefragment);

	    if (currentFrag != null)
	        transaction.remove(currentFrag);

	    transaction.commit();

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) 
//		{
//			case R.id.menu_about: {
//				new About(this).show();
//				return true;
//			}
//			case R.id.menu_credits: {
//				new Credits(this).show();
//				return true;
//			}
//		}
		return false;
	}
	
	
}