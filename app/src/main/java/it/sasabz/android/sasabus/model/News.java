package it.sasabz.android.sasabus.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public final class News implements Parcelable {

    @SerializedName("id")
    private final int id;

    @SerializedName("title")
    private final String title;

    @SerializedName("message")
    private final String message;

    @SerializedName("zone")
    private String zone;

    private boolean highlighted;

    private News(Parcel in) {
        id = in.readInt();
        title = in.readString();
        message = in.readString();
        highlighted = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(message);
        dest.writeByte((byte) (highlighted ? 1 : 0));
    }

    public CharSequence getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlight() {
        highlighted = true;
    }

    public int getId() {
        return id;
    }

    public String getZone() {
        return zone;
    }

    public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", zone='" + zone + '\'' +
                ", highlighted=" + highlighted +
                '}';
    }
}