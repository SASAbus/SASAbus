package com.google.android.gms.analytics;

public class Transaction {

    public static class Builder {

        public Builder(String orderId, double totalCost) {
        }

        public Transaction build() {
            return new Transaction();
        }
    }
}