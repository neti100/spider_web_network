package lcl.android.spider.web.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by NAVER on 2017-06-20.
 */

public class GroupSetting {

    private String groupName;
    private boolean autoSend;
    private List<Contact> contactList;

    public GroupSetting() {
    }

    public GroupSetting(String groupName, List<Contact> contactList) {
        this.groupName = groupName;
        this.contactList = contactList;
    }

    public GroupSetting(String groupName, List<Contact> contactList, boolean autoSend) {
        this.groupName = groupName;
        this.contactList = contactList;
        this.autoSend = autoSend;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isAutoSend() {
        return autoSend;
    }

    public void setAutoSend(boolean autoSend) {
        this.autoSend = autoSend;
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
