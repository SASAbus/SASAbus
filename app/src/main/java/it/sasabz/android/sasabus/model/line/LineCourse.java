package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineCourse implements Parcelable {
    private final int id;
    private final String busStop;
    private final String munic;
    private final String time;
    private final boolean isActive;
    private final boolean dot;

    public LineCourse(int id, String busStop, String munic, String time, boolean isActive, boolean dot) {
        this.id = id;
        this.busStop = busStop;
        this.munic = munic;
        this.time = time;
        this.isActive = isActive;
        this.dot = dot;
    }

    private LineCourse(Parcel in) {
        id = in.readInt();
        busStop = in.readString();
        munic = in.readString();
        time = in.readString();

        isActive = in.readByte() != 0;
        dot = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(busStop);
        dest.writeString(munic);
        dest.writeString(time);

        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeByte((byte) (dot ? 1 : 0));
    }

    public int getId() {
        return id;
    }

    public String getBusStop() {
        return busStop;
    }

    public String getMunic() {
        return munic;
    }

    public String getTime() {
        return time;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isDot() {
        return dot;
    }

    public static final Parcelable.Creator<LineCourse> CREATOR = new Parcelable.Creator<LineCourse>() {

        @Override
        public LineCourse createFromParcel(Parcel in) {
            return new LineCourse(in);
        }

        @Override
        public LineCourse[] newArray(int size) {
            return new LineCourse[size];
        }
    };
}