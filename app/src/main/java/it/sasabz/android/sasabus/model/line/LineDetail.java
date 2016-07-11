package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDetail implements Parcelable {
    private final String currentStation;
    private final int delay;
    private final String lastStation;
    private final String lastTime;
    private final String additionalData;
    private final int vehicle;
    private final boolean color;

    public LineDetail(String currentStation, int delay, String lastStation, String lastTime,
                      String additionalData, int vehicle, boolean color) {

        this.currentStation = currentStation;
        this.delay = delay;
        this.lastStation = lastStation;
        this.lastTime = lastTime;
        this.additionalData = additionalData;
        this.vehicle = vehicle;
        this.color = color;
    }

    private LineDetail(Parcel in) {
        currentStation = in.readString();
        delay = in.readInt();
        lastStation = in.readString();
        lastTime = in.readString();
        additionalData = in.readString();
        vehicle = in.readInt();
        color = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currentStation);
        dest.writeInt(delay);
        dest.writeString(lastStation);
        dest.writeString(lastTime);
        dest.writeString(additionalData);
        dest.writeInt(vehicle);

        dest.writeByte((byte) (color ? 1 : 0));
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public int getDelay() {
        return delay;
    }

    public String getLastStation() {
        return lastStation;
    }

    public CharSequence getLastTime() {
        return lastTime;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public int getVehicle() {
        return vehicle;
    }

    public boolean isColor() {
        return color;
    }

    public static final Parcelable.Creator<LineDetail> CREATOR = new Parcelable.Creator<LineDetail>() {

        @Override
        public LineDetail createFromParcel(Parcel in) {
            return new LineDetail(in);
        }

        @Override
        public LineDetail[] newArray(int size) {
            return new LineDetail[size];
        }
    };
}