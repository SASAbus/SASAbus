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

package it.sasabz.android.sasabus.network.auth.jjwt.impl.crypto;

import java.security.Key;

import it.sasabz.android.sasabus.network.auth.jjwt.SignatureAlgorithm;
import it.sasabz.android.sasabus.network.auth.jjwt.lang.Assert;

class DefaultSignatureValidatorFactory implements SignatureValidatorFactory {

    static final SignatureValidatorFactory INSTANCE = new DefaultSignatureValidatorFactory();

    @Override
    public SignatureValidator createSignatureValidator(SignatureAlgorithm alg, Key key) {
        Assert.notNull(alg, "SignatureAlgorithm cannot be null.");
        Assert.notNull(key, "Signing Key cannot be null.");

        switch (alg) {
            case RS256:
            case RS384:
            case RS512:
            case PS256:
            case PS384:
            case PS512:
                return new RsaSignatureValidator(alg, key);
            default:
                throw new IllegalArgumentException("The '" + alg.name() + "' algorithm cannot be used for signing.");
        }
    }
}
