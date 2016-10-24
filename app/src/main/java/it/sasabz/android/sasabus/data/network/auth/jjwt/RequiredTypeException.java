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

package it.sasabz.android.sasabus.data.network.auth.jjwt;

/**
 * Exception thrown when {@link Claims#get(String, Class)} is called and the value does not match the type of the
 * {@code Class} argument.
 *
 * @since 0.6
 */
public class RequiredTypeException extends JwtException {
    private static final long serialVersionUID = -5248183277332272778L;

    public RequiredTypeException(String message) {
        super(message);
    }
}
