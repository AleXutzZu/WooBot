package me.alexutzzu.woobot.utils.pojos.levels;

public class UserPOJO {
    public UserPOJO(){

    }
    private String userID;

    private long userXP;

    private long userLevel;

    private String currentRoleID;


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public long getUserXP() {
        return userXP;
    }

    public void setUserXP(long userXP) {
        this.userXP = userXP;
    }

    public long getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(long userLevel) {
        this.userLevel = userLevel;
    }

    public String getCurrentRoleID() {
        return currentRoleID;
    }

    public void setCurrentRoleID(String currentRoleID) {
        this.currentRoleID = currentRoleID;
    }
}
