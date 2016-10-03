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

import io.realm.annotations.RealmModule;

/**
 * Indicates which {@link io.realm.RealmObject} belong to the user data realm database.
 * This module itself has no use.
 *
 * @author Alex Lardschneider
 */
@RealmModule(classes = {
        Beacon.class,
        EarnedBadge.class,
        FavoriteBusStop.class,
        FavoriteLine.class,
        FilterLine.class,
        PlannedTrip.class,
        RecentRoute.class,
})
public class UserDataModule {
}