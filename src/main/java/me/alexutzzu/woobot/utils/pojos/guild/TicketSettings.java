package me.alexutzzu.woobot.utils.pojos.guild;

public class TicketSettings {

    private String supportCategoryID = null;

    private String supportTeamRoleID = null;

    private String ticketLogsChannelID = null;

    public String getSupportCategoryID() {
        return supportCategoryID;
    }

    public void setSupportCategoryID(String supportCategoryID) {
        this.supportCategoryID = supportCategoryID;
    }

    public String getSupportTeamRoleID() {
        return supportTeamRoleID;
    }

    public void setSupportTeamRoleID(String supportTeamRoleID) {
        this.supportTeamRoleID = supportTeamRoleID;
    }

    public String getTicketLogsChannelID() {
        return ticketLogsChannelID;
    }

    public void setTicketLogsChannelID(String ticketLogsChannelID) {
        this.ticketLogsChannelID = ticketLogsChannelID;
    }
}
