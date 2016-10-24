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

package it.sasabz.android.sasabus.data.network.auth.jjwt.impl;

import it.sasabz.android.sasabus.data.network.auth.jjwt.Jws;
import it.sasabz.android.sasabus.data.network.auth.jjwt.JwsHeader;

class DefaultJws<B> implements Jws<B> {

    private final JwsHeader<?> header;
    private final B body;
    private final String signature;

    DefaultJws(JwsHeader<?> header, B body, String signature) {
        this.header = header;
        this.body = body;
        this.signature = signature;
    }

    @Override
    public JwsHeader<?> getHeader() {
        return header;
    }

    @Override
    public B getBody() {
        return body;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "header=" + header + ",body=" + body + ",signature=" + signature;
    }
}