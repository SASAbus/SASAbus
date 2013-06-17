/**
 *
 * About.java
 * 
 * Created: Jun 21, 2011 12:56:09 AM
 * 
 * Copyright (C) 2011 Paolo Dongilli and Markus Windegger
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
package it.sasabz.android.sasabus.classes.dialogs;

import it.sasabz.android.sasabus.R;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;


public class About {

	private Activity mActivity;

	public About(Activity context) {
		mActivity = context;
	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mActivity.getPackageManager().getPackageInfo(
					mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	public void show() {
		PackageInfo versionInfo = getPackageInfo();

		// Show the About Dialog
		String title = mActivity.getString(R.string.app_name) + " v"
				+ versionInfo.versionName;

		String currentYear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
		String firstCopyrightYear = mActivity.getString(R.string.copyright_first_year);
		String copyrightYearInterval = firstCopyrightYear;
		if (currentYear.compareTo(firstCopyrightYear) > 0) {
			copyrightYearInterval = firstCopyrightYear + "-" + currentYear;
		}
		// Includes the updates as well so users know what changed.
		String message =
		// mActivity.getString(R.string.updates) +
		String.format(mActivity.getString(R.string.copyright),copyrightYearInterval) + "\n\n"
				+ mActivity.getString(R.string.supp_email) + "\n\n"
				+ mActivity.getString(R.string.license) + "\n\n"
				+ mActivity.getString(R.string.disclaimer) + "\n\n"
				+ mActivity.getString(R.string.gpl);

		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
				.setTitle(title).setMessage(message).setPositiveButton(
						android.R.string.ok, new Dialog.OnClickListener() {

							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
							}
						});
		builder.create().show();
	}

}
