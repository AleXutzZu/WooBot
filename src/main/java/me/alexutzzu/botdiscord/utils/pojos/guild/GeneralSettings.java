package me.alexutzzu.botdiscord.utils.pojos.guild;

import java.util.ArrayList;
import java.util.List;

public class GeneralSettings {

    private String commandPrefix = "!";

    private List<String> blacklistedChannelsID = new ArrayList<>();


    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public List<String> getBlacklistedChannelsID() {
        return blacklistedChannelsID;
    }

    public void setBlacklistedChannelsID(List<String> blacklistedChannelsID) {
        this.blacklistedChannelsID = blacklistedChannelsID;
    }
}
