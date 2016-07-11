package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDrivingContent implements Parcelable {
    private final String busStop;
    private final int delay;

    public LineDrivingContent(String busStop, int delay) {
        this.busStop = busStop;
        this.delay = delay;
    }

    private LineDrivingContent(Parcel in) {
        busStop = in.readString();
        delay = in.readInt();
    }

    public CharSequence getBusStop() {
        return busStop;
    }

    public int getDelay() {
        return delay;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busStop);
        dest.writeInt(delay);
    }

    public static final Parcelable.Creator<LineDrivingContent> CREATOR = new Parcelable.Creator<LineDrivingContent>() {

        @Override
        public LineDrivingContent createFromParcel(Parcel in) {
            return new LineDrivingContent(in);
        }

        @Override
        public LineDrivingContent[] newArray(int size) {
            return new LineDrivingContent[size];
        }
    };
}