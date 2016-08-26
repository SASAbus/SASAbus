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

package it.sasabz.android.sasabus.network.auth.jjwt.impl;

import java.util.Map;

import it.sasabz.android.sasabus.network.auth.jjwt.Header;

@SuppressWarnings("unchecked")
public class DefaultHeader<T extends Header<T>> extends JwtMap implements Header<T> {

    public DefaultHeader() {
    }

    public DefaultHeader(Map<String, Object> map) {
        super(map);
    }

    @Override
    public String getType() {
        return getString(TYPE);
    }

    @Override
    public T setType(String typ) {
        setValue(TYPE, typ);
        return (T) this;
    }
}
