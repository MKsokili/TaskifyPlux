package com.example.taskmanager.CustomerClass;

public class UserClass
{
    String userName, userEmail, userId, userRole;

    // empty constructor
    public UserClass() {

    }

    public UserClass(String userName, String userEmail, String userId, String userRole) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userRole = userRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
