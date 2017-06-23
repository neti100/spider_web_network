package lcl.android.spider.web.network.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by NAVER on 2017-06-20.
 */

public class GroupSetting {

    private String groupName;
    private String messagePrefix;
    private List<Contact> contactList;

    public GroupSetting() {
    }

    public GroupSetting(String groupName, String messagePrefix, List<Contact> contactList) {
        this.groupName = groupName;
        this.messagePrefix = messagePrefix;
        this.contactList = contactList;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
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
