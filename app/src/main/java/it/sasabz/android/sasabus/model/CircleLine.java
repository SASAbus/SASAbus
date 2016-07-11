package it.sasabz.android.sasabus.model;

import android.os.Parcel;
import android.os.Parcelable;

public class CircleLine implements Parcelable {

    private final int id;
    private boolean selected;

    public CircleLine(int id) {
        this.id = id;
    }

    private CircleLine(Parcel in) {
        id = in.readInt();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CircleLine> CREATOR = new Creator<CircleLine>() {
        @Override
        public CircleLine createFromParcel(Parcel in) {
            return new CircleLine(in);
        }

        @Override
        public CircleLine[] newArray(int size) {
            return new CircleLine[size];
        }
    };

    public int getId() {
        return id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}