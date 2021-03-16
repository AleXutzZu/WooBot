package me.alexutzzu.woobot.utils.pojos.guild;

public class WelcomeSettings {
    private String joinMessage = "Hey {user}! Welcome to {server}!";

    private String leaveMessage = "Unfortunately {user} has left {server}. Wish them well.";

    private String messageChannelID = null;

    private String defaultRoleID = null;

    private boolean useWelcome = false;


    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }

    public String getMessageChannelID() {
        return messageChannelID;
    }

    public void setMessageChannelID(String messageChannelID) {
        this.messageChannelID = messageChannelID;
    }

    public String getDefaultRoleID() {
        return defaultRoleID;
    }

    public void setDefaultRoleID(String defaultRoleID) {
        this.defaultRoleID = defaultRoleID;
    }

    public boolean isUseWelcome() {
        return useWelcome;
    }

    public void setUseWelcome(boolean useWelcome) {
        this.useWelcome = useWelcome;
    }
}
