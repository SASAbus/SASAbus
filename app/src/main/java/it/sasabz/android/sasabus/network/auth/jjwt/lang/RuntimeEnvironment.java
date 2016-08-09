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
