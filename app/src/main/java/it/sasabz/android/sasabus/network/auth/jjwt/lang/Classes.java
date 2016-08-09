package it.sasabz.android.sasabus.network.auth.jjwt.lang;

final class Classes {

    /**
     * @since 0.1
     */
    private static final ClassLoaderAccessor THREAD_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }
    };

    /**
     * @since 0.1
     */
    private static final ClassLoaderAccessor CLASS_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() {
            return Classes.class.getClassLoader();
        }
    };

    /**
     * @since 0.1
     */
    private static final ClassLoaderAccessor SYSTEM_CL_ACCESSOR = new ExceptionIgnoringAccessor() {
        @Override
        protected ClassLoader doGetClassLoader() {
            return ClassLoader.getSystemClassLoader();
        }
    };

    private Classes() {
    }

    /**
     * Attempts to load the specified class name from the current thread's
     * {@link Thread#getContextClassLoader() context class loader}, then the
     * current ClassLoader ({@code Classes.class.getClassLoader()}), then the system/application
     * ClassLoader ({@code ClassLoader.getSystemClassLoader()}, in that order.  If any of them cannot locate
     * the specified class, an {@code UnknownClassException} is thrown (our RuntimeException equivalent of
     * the JRE's {@code ClassNotFoundException}.
     *
     * @param fqcn the fully qualified class name to load
     * @return the located class
     * @throws UnknownClassException if the class cannot be found.
     */
    static Class<?> forName(String fqcn) throws UnknownClassException {
        Class<?> clazz = THREAD_CL_ACCESSOR.loadClass(fqcn);

        if (clazz == null) {
            clazz = CLASS_CL_ACCESSOR.loadClass(fqcn);
        }

        if (clazz == null) {
            clazz = SYSTEM_CL_ACCESSOR.loadClass(fqcn);
        }

        if (clazz == null) {
            String msg = "Unable to load class named [" + fqcn + "] from the thread context, current, or " +
                    "system/application ClassLoaders.  All heuristics have been exhausted.  Class could not be found.";

            if (fqcn != null && fqcn.startsWith("com.stormpath.sdk.impl")) {
                msg += "  Have you remembered to include the stormpath-sdk-impl .jar in your runtime classpath?";
            }

            throw new UnknownClassException(msg);
        }

        return clazz;
    }

    static boolean isAvailable(String fullyQualifiedClassName) {
        try {
            forName(fullyQualifiedClassName);
            return true;
        } catch (UnknownClassException e) {
            return false;
        }
    }

    public static <T> T newInstance(Class<T> clazz) {
        if (clazz == null) {
            String msg = "Class method parameter cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new InstantiationException("Unable to instantiate class [" + clazz.getName() + ']', e);
        }
    }

    /**
     * @since 1.0
     */
    private interface ClassLoaderAccessor {
        Class<?> loadClass(String fqcn);
    }

    /**
     * @since 1.0
     */
    private abstract static class ExceptionIgnoringAccessor implements ClassLoaderAccessor {

        @Override
        public Class<?> loadClass(String fqcn) {
            Class<?> clazz = null;
            ClassLoader cl = getClassLoader();
            if (cl != null) {
                try {
                    clazz = cl.loadClass(fqcn);
                } catch (ClassNotFoundException e) {
                    //Class couldn't be found by loader
                }
            }
            return clazz;
        }

        final ClassLoader getClassLoader() {
            try {
                return doGetClassLoader();
            } catch (Throwable t) {
                //Unable to get ClassLoader
            }
            return null;
        }

        protected abstract ClassLoader doGetClassLoader();
    }
}
