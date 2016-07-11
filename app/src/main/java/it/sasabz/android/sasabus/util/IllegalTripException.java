package it.sasabz.android.sasabus.util;

/**
 * Helper exception class which gets thrown when the beacon handler
 * attempted to insert a invalid trip into the database.
 *
 * @author Alex Lardschneider
 */
public class IllegalTripException extends IllegalStateException {

    private static final long serialVersionUID = 951603347670007642L;

    public IllegalTripException(String detailMessage) {
        super(detailMessage);
    }
}