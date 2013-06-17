/**
 *
 * OfflineActivity.java
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
 * This activity is responsable for all the operations allowed during offline-mode.
 * It's the space for all fragments related to it.
 *
 */
package it.sasabz.android.sasabus;

import it.sasabz.android.sasabus.classes.dialogs.About;
import it.sasabz.android.sasabus.classes.dialogs.Credits;
import it.sasabz.android.sasabus.fragments.BacinoFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class OfflineActivity extends FragmentActivity{

	
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
		fragment = new BacinoFragment();
		ft.add(R.id.onlinefragment, fragment);
		ft.commit();
		fragmentManager.executePendingTransactions();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.optionmenu, menu);
   	 	return true;
	}

	/**
	 * This method adds some functionalities to some keys, for example the back key,
	 * which is not catched by the fragment manager and only by activity.
	 */
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

	
	/**
	 * removes the current fragment and shows the previous fragment
	 */
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
		switch (item.getItemId()) 
		{
			case R.id.menu_about: {
				new About(this).show();
				return true;
			}
			case R.id.menu_credits: {
				new Credits(this).show();
				return true;
			}
		}
		return false;
	}
	
}
