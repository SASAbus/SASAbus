package com.google.android.gms.analytics;

public class Item {

    public static class Builder {

        public Builder(String orderId, String itemSKU, double itemPrice, long itemCount) {
        }

        public Item build() {
            return new Item();
        }
    }
}