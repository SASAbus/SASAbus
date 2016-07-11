package it.sasabz.android.sasabus.model.line;

import android.os.Parcel;
import android.os.Parcelable;

public class LineDisruption implements Parcelable {

    private final int id;
    private boolean selected;

    public LineDisruption(int id) {
        this.id = id;
    }

    private LineDisruption(Parcel in) {
        id = in.readInt();
        selected = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(selected ? 1 : 0);
    }

    public boolean isSelected() {
        return selected;
    }

    public int getId() {
        return id;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public static final Creator<LineDisruption> CREATOR = new Creator<LineDisruption>() {

        @Override
        public LineDisruption createFromParcel(Parcel in) {
            return new LineDisruption(in);
        }

        @Override
        public LineDisruption[] newArray(int size) {
            return new LineDisruption[size];
        }
    };
}