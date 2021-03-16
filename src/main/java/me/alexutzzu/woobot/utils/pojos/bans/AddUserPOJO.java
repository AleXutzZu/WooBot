package me.alexutzzu.woobot.utils.pojos.bans;

public class AddUserPOJO {
    public AddUserPOJO(){

    }
    private String addedUserID;

    private String addedByUserID;

    private String reason = "No reason provided";


    public String getAddedUserID() {
        return addedUserID;
    }

    public void setAddedUserID(String addedUserID) {
        this.addedUserID = addedUserID;
    }

    public String getAddedByUserID() {
        return addedByUserID;
    }

    public void setAddedByUserID(String addedByUserID) {
        this.addedByUserID = addedByUserID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
