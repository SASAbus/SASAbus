/*
 * Copyright (C) 2016 David Dejori, Alex Lardschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.sasabz.android.sasabus.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.crashlytics.android.answers.SignUpEvent;

import it.sasabz.android.sasabus.BuildConfig;

public final class AnswersHelper {

    private AnswersHelper() {
    }

    public static void logProfileAction(String action) {
        CustomEvent event = new CustomEvent("EcoPointsProfile")
                .putCustomAttribute("Category", action);

        Answers.getInstance().logCustom(event);
    }

    public static void logLoginSuccess() {
        if (!BuildConfig.DEBUG) {
            LoginEvent event = new LoginEvent()
                    .putSuccess(true);

            Answers.getInstance().logLogin(event);
        }
    }

    public static void logLoginError(String error) {
        if (!BuildConfig.DEBUG) {
            LoginEvent event = new LoginEvent()
                    .putSuccess(false)
                    .putCustomAttribute("Error", error);

            Answers.getInstance().logLogin(event);
        }
    }

    public static void logSignUpSuccess() {
        if (!BuildConfig.DEBUG) {
            SignUpEvent event = new SignUpEvent()
                    .putSuccess(true);

            Answers.getInstance().logSignUp(event);
        }
    }

    public static void logSignUpError(String error) {
        if (!BuildConfig.DEBUG) {
            SignUpEvent event = new SignUpEvent()
                    .putSuccess(false)
                    .putCustomAttribute("Error", error);

            Answers.getInstance().logSignUp(event);
        }
    }
}
