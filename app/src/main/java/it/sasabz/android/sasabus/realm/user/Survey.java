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

package it.sasabz.android.sasabus.realm.user;

import io.realm.RealmObject;

/**
 * Holds the batched surveys. Batched surveys are surveys which couldn't be sent to the server
 * at the time the user filled them out.
 *
 * Those surveys will be sent automatically the next time the app performs a sync and will then
 * be removed from the database. Ideally we wan't this table to be empty all the time.
 *
 * @author Alex Lardschneider
 */
public class Survey extends RealmObject {

    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
