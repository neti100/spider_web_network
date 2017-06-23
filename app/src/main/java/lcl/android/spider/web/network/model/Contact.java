package lcl.android.spider.web.network.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by NAVER on 2017-06-20.
 */

public class Contact implements Parcelable, Serializable {
    public static ContactParcelableCreator CREATOR = new ContactParcelableCreator();

    private String phoneNumber;
    private String name;
    private boolean isChecked;

    private ContactType type;

    public Contact() {
    }

    public Contact(String phoneNumber, String name, ContactType type) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.type = type;
    }

    public Contact(String phoneNumber, String name, boolean isChecked, ContactType type) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.isChecked = isChecked;
        this.type = type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public ContactType getType() {
        return type;
    }

    public void setType(ContactType type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        return phoneNumber != null ? phoneNumber.equals(contact.phoneNumber) : contact.phoneNumber == null;

    }

    @Override
    public int hashCode() {
        return phoneNumber != null ? phoneNumber.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", isChecked=" + isChecked +
                ", type=" + type +
                '}';
    }
}
