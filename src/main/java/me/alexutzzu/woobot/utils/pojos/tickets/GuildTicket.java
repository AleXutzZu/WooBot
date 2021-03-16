package me.alexutzzu.woobot.utils.pojos.tickets;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class GuildTicket {
    public GuildTicket(){

    }
    public GuildTicket(String guildID){
        this.guildID = guildID;
    }
    @BsonProperty("_id")
    private ObjectId id;

    @BsonProperty("userID")
    private String userID;

    @BsonProperty("guildID")
    private String guildID;

    @BsonProperty("channelID")
    private String channelID;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getGuildID() {
        return guildID;
    }

    public void setGuildID(String guildID) {
        this.guildID = guildID;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
