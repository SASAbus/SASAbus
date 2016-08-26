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

package it.sasabz.android.sasabus.network.auth.jjwt.lang;

import java.security.Provider;
import java.security.Security;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RuntimeEnvironment {

    private static final String BC_PROVIDER_CLASS_NAME = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    private static final AtomicBoolean bcLoaded = new AtomicBoolean(false);

    public static final boolean BOUNCY_CASTLE_AVAILABLE = Classes.isAvailable(BC_PROVIDER_CLASS_NAME);

    private RuntimeEnvironment() {
    }

    public static void enableBouncyCastleIfPossible() {
        if (bcLoaded.get()) {
            return;
        }

        try {
            Class<?> clazz = Classes.forName(BC_PROVIDER_CLASS_NAME);

            //check to see if the user has already registered the BC provider:

            Provider[] providers = Security.getProviders();

            for (Provider provider : providers) {
                if (clazz.isInstance(provider)) {
                    bcLoaded.set(true);
                    return;
                }
            }

            //bc provider not enabled - add it:
            Security.addProvider((Provider) Classes.newInstance(clazz));
            bcLoaded.set(true);

        } catch (UnknownClassException e) {
            //not available
        }
    }

    static {
        enableBouncyCastleIfPossible();
    }
}
