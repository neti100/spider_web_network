package lcl.android.spider.web.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by NAVER on 2017-06-20.
 */

public class GroupSetting {

    private String groupName;
    private List<Contact> contactList;

    public GroupSetting() {
    }

    public GroupSetting(String groupName, List<Contact> contactList) {
        this.groupName = groupName;
        this.contactList = contactList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    @JsonIgnore
    public String getSharedPreferenceKey() {
        return Constants.GROUP_SETTING_KEY_PREFIX + groupName;
    }
}
