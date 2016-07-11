package com.google.android.gms.analytics;

public class HitBuilders {

    public static class HitBuilder {

        public void build() {
        }
    }

    public static class ScreenViewBuilder {

        public HitBuilder build() {
            return new HitBuilder();
        }
    }

    public static class EventBuilder {

        public EventBuilder() {
        }

        public HitBuilders.EventBuilder setCategory(java.lang.String s) {
            return this;
        }

        public HitBuilders.EventBuilder setAction(java.lang.String s) {
            return this;
        }

        public HitBuilders.EventBuilder setLabel(java.lang.String s) {
            return this;
        }

        public HitBuilders.EventBuilder setValue(long l) {
            return this;
        }

        public HitBuilder build() {
            return null;
        }
    }
}