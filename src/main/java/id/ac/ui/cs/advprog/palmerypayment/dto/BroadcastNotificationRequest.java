package id.ac.ui.cs.advprog.palmerypayment.dto;

import java.util.List;

public class BroadcastNotificationRequest {

    private String title;
    private String description;
    private List<String> userIds;
    private String roleTarget;
    private Boolean all;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getRoleTarget() {
        return roleTarget;
    }

    public void setRoleTarget(String roleTarget) {
        this.roleTarget = roleTarget;
    }

    public Boolean getAll() {
        return all;
    }

    public void setAll(Boolean all) {
        this.all = all;
    }
}
