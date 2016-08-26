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
import io.realm.annotations.Ignore;

/**
 * Holds all the trips which need to be deleted (or already are in the app), but their deletion
 * couldn't be sent to the server, most probably because of a connection issue.
 *
 * Trips and planned trips are collected together in the same table and can be identified by
 * their {@link #type}, which can be either {@link #TYPE_TRIP} or {@link #TYPE_PLANNED_TRIP}.
 *
 * The deletion request will be sent automatically the next time the app syncs and the
 * corresponding entry will be removed from the table. Ideally we want this table to be empty.
 *
 * @author Alex Lardschneider
 */
public class TripToDelete extends RealmObject {

    @Ignore
    public static final int TYPE_TRIP = 0;

    @Ignore
    public static final int TYPE_PLANNED_TRIP = 1;

    private String hash;
    private int type;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
