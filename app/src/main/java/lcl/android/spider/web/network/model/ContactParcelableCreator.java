package lcl.android.spider.web.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by NAVER on 2017-06-21.
 */

public class ContactParcelableCreator implements Parcelable.Creator<Contact>, Serializable {

    public Contact createFromParcel(Parcel in) {
        return (Contact) in.readSerializable();
    }

    @Override
    public Contact[] newArray(int size) {
        return new Contact[size];
    }
}
