package com.gs.buluo.app.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fs on 2016/12/13.
 */
public class PropertyBeen implements Parcelable {
    public String communityID;
    public String communityName;
    public String enterpriseID;
    public String enterpriseName;
    public String name;

    public PropertyBeen() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.communityID);
        dest.writeString(this.communityName);
        dest.writeString(this.enterpriseID);
        dest.writeString(this.enterpriseName);
        dest.writeString(this.name);
    }

    protected PropertyBeen(Parcel in) {
        this.communityID = in.readString();
        this.communityName = in.readString();
        this.enterpriseID = in.readString();
        this.enterpriseName = in.readString();
        this.name = in.readString();
    }

    public static final Creator<PropertyBeen> CREATOR = new Creator<PropertyBeen>() {
        @Override
        public PropertyBeen createFromParcel(Parcel source) {
            return new PropertyBeen(source);
        }

        @Override
        public PropertyBeen[] newArray(int size) {
            return new PropertyBeen[size];
        }
    };
}
